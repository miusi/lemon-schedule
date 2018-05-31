package com.gabry.job.manager.actor

import java.sql.SQLIntegrityConstraintViolationException

import akka.actor.{ActorRef, Props, Status}
import com.gabry.job.core.actor.SimpleActor
import com.gabry.job.core.command.{JobSchedulerCommand, JobTrackerCommand}
import com.gabry.job.core.event.JobTrackerEvent
import com.gabry.job.core.po.{DependencyPo, JobPo}
import com.gabry.job.db.proxy.DataAccessProxyException
import com.gabry.job.db.proxy.command.DatabaseCommand
import com.gabry.job.db.proxy.event.DatabaseEvent
import com.gabry.job.manager.node.JobTrackerNode

import scala.concurrent.ExecutionContextExecutor

/**
  * Created by gabry on 2018/5/2 9:38
  */
object JobTrackerActor{
  def props(dataAccessProxy:ActorRef):Props =
    Props.create(classOf[JobTrackerActor],dataAccessProxy)
}
class JobTrackerActor private (dataAccessProxy:ActorRef) extends SimpleActor{

  private implicit lazy val databaseIoExecutionContext: ExecutionContextExecutor = context.system.dispatchers.lookup("akka.actor.database-io-dispatcher")

  private def exceptionEventReceive:Receive = {
    /**
      * 作业插入失败
      */
    case Status.Failure(DataAccessProxyException(
    DatabaseCommand.Insert(insertingJobPo:JobPo,_,originCommand @ JobTrackerCommand.SubmitJob(job,_,replyTo))
    ,reason)) =>
      reason match {
        // 主键冲突类的失败进行特殊处理
        case _:SQLIntegrityConstraintViolationException if job.replaceIfExist =>
          log.warning("作业主键冲突，且允许替换原有作业")
          dataAccessProxy ! DatabaseCommand.Select(insertingJobPo,self,originCommand)

        case _:SQLIntegrityConstraintViolationException =>
          log.warning("Job already exists")
          replyTo ! JobTrackerEvent.JobSubmitFailed("Job already exists")

        case unKnownReason:Throwable =>
          log.error(s"Job insert failed, unknown reason: $unKnownReason")
          replyTo ! JobTrackerEvent.JobSubmitFailed(s"Job insert failed, unknown reason: $unKnownReason")
      }

    case Status.Failure(
    DataAccessProxyException(DatabaseCommand.Update(_:JobPo,_:JobPo,_,originCommand: JobTrackerCommand.SubmitJob)
    ,reason)) =>
      log.error(reason,reason.getMessage)
      log.error(s"Replacing Job .Job update failed,reason: $reason")
      originCommand.replyTo ! JobTrackerEvent.JobSubmitFailed(s"Replacing Job .Job update failed,reason: $reason")

    case Status.Failure(DataAccessProxyException(DatabaseCommand.Select(_:JobPo,_,originCommand:JobTrackerCommand.SubmitJob),reason)) =>
      log.error(reason,reason.getMessage)
      log.error(s"Replacing Job .Old job select failed,reason: $reason")
      originCommand.replyTo ! JobTrackerEvent.JobSubmitFailed(s"Replacing Job .Old job select failed,reason: $reason")

    case Status.Failure(
    DataAccessProxyException(DatabaseCommand.Insert(_:Array[DependencyPo],_,originCommand:JobTrackerCommand.SubmitJob)
    ,reason)) =>

      log.error(reason,reason.getMessage)
      log.warning(s"Dependency insert failed,reason: ${reason.getMessage}")
      originCommand.replyTo ! JobTrackerEvent.JobSubmitFailed(s"Dependency insert failed,reason: ${reason.getMessage}")
  }

  private def dbEventReceive:Receive = {
    /**
      * Job插入成功
      * 成功后，将Dependency插入数据库，然后将DependencyInserted消息pipe给self
      */
    case DatabaseEvent.Inserted(Some(insertedJobPo:JobPo),originCmd:JobTrackerCommand.SubmitJob) =>
      log.debug(s"Job inserted to db $insertedJobPo")
      val dependencyPos = originCmd.dependency.map(r=>JobTrackerNode.createDependencyPo(insertedJobPo.uid,r))
      dataAccessProxy ! DatabaseCommand.Insert(dependencyPos,self,originCmd)

    /**
      * Dependency插入成功
      * 然后告诉发送方作业提交结果
      */
    case DatabaseEvent.BatchInserted(insertedNum,headDependency:Option[DependencyPo],JobTrackerCommand.SubmitJob(job,dependency,replyTo)) =>
      // 插入成功后，给调度器发送调度命令，并告知客户端提交结果
      log.debug(s"Dependency [${dependency.mkString(",")}] inserted to db")
      // 将消息发送给父节点，由父节点处理调度消息。因为调度器节点信息只有父节点有
      context.parent ! JobTrackerCommand.ScheduleJob(job,replyTo)

    /**
      * Job因为主键冲突插入失败，且允许替换原来作业时，查出来的原Job信息
      */
    case DatabaseEvent.Selected(selectedJobPo:Option[JobPo],originCommand @ JobTrackerCommand.SubmitJob(job,dependency,replyTo)) =>
      log.debug(s"Replacing Job. old job is $selectedJobPo")
      selectedJobPo match {
        case Some(oldJob) =>
          // TODO: 如何在原调度器调度过程中，异步终止调度，然后重新调度呢？这是个重要的问题
          // 采取异步的机制，在旧调度器停止调度该作业之后，发消息个JobTracker，由JobTracker重新调度
          // 但要考虑各种异常情况，例如旧调度器已经异常终止，无法响应消息；就调度器暂时无法终止调度
          oldJob.schedulerNode.map( node => context.actorSelection(node) ).foreach{ oldScheduler =>
            log.warning(s"原有作业找到，现在停止原调度器调度$oldJob")
            oldScheduler ! JobSchedulerCommand.StopScheduleJob(oldJob)
          }
          // 更新目前job到数据库，并重新调度
          val updatingJobPo:JobPo = job
          // 替换原命令行作业ID
          dataAccessProxy ! DatabaseCommand.Update(oldJob,updatingJobPo,self,originCommand)

        case None =>

          replyTo ! JobTrackerEvent.JobSubmitFailed("Replacing Job but old job not found")
      }
    /**
      * Job因为主键冲突插入失败，且允许替换原来作业时，更新原来作业信息
      */
    case DatabaseEvent.Updated(oldJobPo:JobPo,updatedJobPo:Option[JobPo],originCommand @ JobTrackerCommand.SubmitJob(_,dependency,_)) =>
      log.debug(s"Replacing Job updated job is $updatedJobPo")

      val dependencyPos = dependency.map(r=>JobTrackerNode.createDependencyPo(oldJobPo.uid,r))
      dataAccessProxy ! DatabaseCommand.Insert(dependencyPos,self,originCommand)
  }
  private def commandEventReceive:Receive = {
    /**
      * 收到客户端提交Job的命令
      * 将Job插入数据库，并将插入的结果，以Inserted[JobPo]事件的形式pipe给self
      */
    case originCmd @ JobTrackerCommand.SubmitJob(job,_,_) =>
      log.debug(s"Receive SubmitJob Command $originCmd")
      val jobPo:JobPo = job
      dataAccessProxy ! DatabaseCommand.Insert(jobPo,self,originCmd)

  }
  /**
    * 用户自定义事件处理函数
    */
  override def userDefineEventReceive: Receive =
    commandEventReceive orElse dbEventReceive orElse exceptionEventReceive
}
