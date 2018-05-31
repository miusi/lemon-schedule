package com.gabry.job.scheduler.actor

import java.sql.SQLIntegrityConstraintViolationException

import akka.actor.{ActorRef, Props, Status}
import com.gabry.job.core.actor.SimpleActor
import com.gabry.job.core.command.JobSchedulerCommand
import com.gabry.job.core.domain.Job
import com.gabry.job.core.po.{JobPo, SchedulePo}
import com.gabry.job.core.tools.UIDGenerator
import com.gabry.job.db.DataTables
import com.gabry.job.db.proxy.DataAccessProxyException
import com.gabry.job.db.proxy.command.DatabaseCommand
import com.gabry.job.db.proxy.event.DatabaseEvent
import com.gabry.job.quartz.{MessageRequireFireTime, MessageWithFireTime, QuartzSchedulerExtension}
import com.gabry.job.utils.{CronGenerator, Utils}

/**
  * Created by gabry on 2018/4/3 11:05
  */
object JobSchedulerActor{
  def props(dataAccessProxy: ActorRef,nodeAnchor:String):Props = Props(new JobSchedulerActor(dataAccessProxy,nodeAnchor))
}
/**
  * 作业调度器。根据提交的Job信息，创建对应的执行计划表
  * @param dataAccessProxy 数据操作代理actor
  * @param nodeAnchor JobSchedulerNode节点信息
  */
class JobSchedulerActor private (dataAccessProxy: ActorRef,nodeAnchor:String)  extends SimpleActor{
  private lazy val scheduler = QuartzSchedulerExtension(context.system)

  /**
    * 调度器调度的周期，配置文件中单位是分钟
    * 调度器在每个周期执行的时候，提前生成下个周期的作业
    */
  private val frequencyInSec = config.getDuration("scheduler.frequency").getSeconds

  private def createJobPo(job:Job,scheduleTime:Long):JobPo =

    JobPo(job.uid,job.name,job.className,job.getMetaJsonString(),job.dataTimeOffset,job.dataTimeOffsetUnit
      ,job.startTime,job.cron,job.priority,job.parallel,job.retryTimes
      ,Some(job.workerNodes.mkString(",")),job.cluster,job.group,job.timeOut,job.replaceIfExist
      ,None ,schedulerNode = Some(nodeAnchor)
      ,scheduleFrequency = Some(frequencyInSec)
      ,lastScheduleTime = Some(scheduleTime) )

  override def preStart(): Unit = {
    super.preStart()
    scheduler.schedule("JobScheduler",self,MessageRequireFireTime(JobSchedulerCommand.ScheduleJobFreq))
    log.info(s"JobSchedulerActor started at $selfAddress")
  }

  override def postStop(): Unit = {
    super.postStop()
    scheduler.shutdown()
  }
  private def getScheduleStopTime(startTime:Long):Long =
    startTime + frequencyInSec * 1000 * 1

  private def scheduleJob(scheduleTime:Long,jobPo: JobPo,originCommand:AnyRef):Unit = {
    var lastTriggerTime = scheduleTime
    val stopTime = getScheduleStopTime(scheduleTime)
    while( lastTriggerTime < stopTime){
      CronGenerator.getNextTriggerTime(jobPo.cron,lastTriggerTime) match {
        case Some(nextTriggerTime) =>
          log.debug(s"generate nextTriggerTime $nextTriggerTime for ${jobPo.name}")
          lastTriggerTime = nextTriggerTime

          val schedulePo = SchedulePo(UIDGenerator.globalUIDGenerator.nextUID(),jobPo.uid,jobPo.priority,jobPo.retryTimes
            ,dispatched = false,nextTriggerTime,nodeAnchor,scheduleTime,succeed = false,
            Utils.calcPostOffsetTime(nextTriggerTime,jobPo.dataTimeOffset,jobPo.dataTimeOffsetUnit),null)

          dataAccessProxy ! DatabaseCommand.Insert(schedulePo,self,originCommand)

        case None =>
          log.info(s"can not generate next trigger time [${jobPo.cron}]")
          lastTriggerTime = stopTime
      }
    }
    // 更新最后一次生成的作业触发时间
    val newJobPo = jobPo.copy(lastGenerateTriggerTime = Some(lastTriggerTime)
      ,lastScheduleTime = Some(scheduleTime),schedulerNode = Some(nodeAnchor))

    dataAccessProxy ! DatabaseCommand.Update(jobPo,newJobPo,self,originCommand)

  }
  override def userDefineEventReceive: Receive = {
    case DatabaseEvent.Inserted(Some(schedulePo:SchedulePo),originEvent)=>
      log.debug(s"Schedule [${schedulePo.uid}] inserted to db for $originEvent")
    case Status.Failure(DataAccessProxyException(DatabaseCommand.Insert(_:SchedulePo,_,_),exception)) =>
      exception match {
        case _:SQLIntegrityConstraintViolationException =>
        // 由于会重新生成作业实例，此处的异常是正常的
        case unacceptableException:Throwable =>
          log.error(s"unacceptableException:$unacceptableException")
          log.error(unacceptableException,unacceptableException.getMessage)
      }
    case DatabaseEvent.Updated(oldRow:JobPo,row:Option[JobPo],originCommand) =>
      log.debug(s"JobPo $oldRow update to $row for $originCommand")
    case Status.Failure(DataAccessProxyException(DatabaseCommand.Update(oldRow:JobPo,row:JobPo,_,originCommand),exception)) =>
      log.error(exception,exception.getMessage)
      log.error(s"JobPo $oldRow update to $row error ,reason: $exception")

    case cmd @ MessageWithFireTime(_,scheduledFireTime) =>
      // 首先根据scheduledFireTime计算当前需要调度的起始时间和终止时间。
      // 从jobs获取最后一次调度的时间，计算未执行的作业的个数，和最后一个需要执行的时间，如果个数小于aheadNum或者待执行作业执行时间跨度小于调度周期，则补满
      // 2018年4月8日09:30:22：上面的方案太复杂，简化一下，先提前一个周期调度。如果某个作业最后一次调度的时间与调度的任务最后一次执行时间相差一个周期，则开始调度
      val scheduleTime = scheduledFireTime.getTime
      log.warning(s"scheduleTime=$scheduleTime")

      dataAccessProxy ! DatabaseCommand.Select((DataTables.JOB,nodeAnchor,scheduleTime,frequencyInSec),self,cmd)

    case DatabaseEvent.Selected(Some(jobPo:JobPo),originCommand @ MessageWithFireTime(_,scheduledFireTime)) =>
      scheduleJob(scheduledFireTime.getTime,jobPo,originCommand)
    /**
      * 收到manager的调度信息，需要与周期性的调度区分开
      */
    case originCmd @ JobSchedulerCommand.ScheduleJob(job,replyTo) =>
      // 登记该作业的调度器信息字段到数据库，然后发送消息，给当前节点生成执行计划表
      log.info(s"开始调度作业 $job")
      // 首先删除已经生成的计划表中未执行的信息，然后重新生成
      val scheduleTime = System.currentTimeMillis()

      // 删除成功后开始生成作业执行计划表
      val jobPo = createJobPo(job,scheduleTime)
      // 更新作业信息
      scheduleJob(jobPo.startTime,jobPo,originCmd)

    case JobSchedulerCommand.StopScheduleJob(job) =>
      log.warning(s"收到停止调度job的消息$job")
    // TODO: 2018年4月12日17:52:59 停止调度job。删掉任务计划表、停止当前作业、发通知给JobTracker
    // TODO: 2018年4月12日17:54:31 Job类中需要增加是否强制停止当前作业的标志，如果不强制停止，会一直等作业执行完毕，那么等待多久呢？

  }
}
