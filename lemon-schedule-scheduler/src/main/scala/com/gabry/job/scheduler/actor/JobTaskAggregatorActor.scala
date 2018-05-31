package com.gabry.job.scheduler.actor

import akka.actor.{ActorRef, Props, Status}
import com.gabry.job.core.actor.SimpleActor
import com.gabry.job.core.command.TaskCommand
import com.gabry.job.core.domain.TaskStatus.TaskStatus
import com.gabry.job.core.domain.{JobContext, TaskStatus, UID}
import com.gabry.job.core.event.{Event, TaskActorEvent, TaskEvent}
import com.gabry.job.core.po.TaskPo
import com.gabry.job.core.tools.UIDGenerator
import com.gabry.job.db.DataTables
import com.gabry.job.db.proxy.DataAccessProxyException
import com.gabry.job.db.proxy.command.DatabaseCommand
import com.gabry.job.db.proxy.event.DatabaseEvent

object JobTaskAggregatorActor{
  def props(dataAccessProxy: ActorRef,nodeAnchor:String):Props = Props(new JobTaskAggregatorActor(dataAccessProxy,nodeAnchor))
}
/**
  * Created by gabry on 2018/4/8 10:25
  * 任务状态聚合类
  */
class JobTaskAggregatorActor private (dataAccessProxy:ActorRef,nodeAnchor:String) extends SimpleActor {


  private def insertTaskPo(from:String,jobContext: JobContext,status:TaskStatus,eventTime:Long,message:Option[String],originEvent:Event):Unit = {
    val taskPo = TaskPo(UIDGenerator.globalUIDGenerator.nextUID(),jobContext.job.uid,jobContext.schedule.uid,jobContext.retryId,from,status,eventTime,message)
    dataAccessProxy ! DatabaseCommand.Insert(taskPo,self,originEvent)
}
  override def userDefineEventReceive: Receive = {
    // 任务开始调度
    case evt @ TaskEvent.Started(taskRunner,jobContext) =>
      insertTaskPo(taskRunner.toString(),jobContext,TaskStatus.Started,evt.at,None,evt)
    case evt @ TaskEvent.Waiting(jobContext) =>
      val from = sender().toString()
      insertTaskPo(from,jobContext,TaskStatus.Waiting,evt.at,None,evt)
    // 任务执行中
    case evt @ TaskEvent.Executing(jobContext) =>
      val from = sender().toString()
      insertTaskPo(from,jobContext,TaskStatus.Executing,evt.at,None,evt)
    // 任务超时
    case evt @ TaskEvent.Timeout(jobContext) =>
      val from = sender().toString()
      insertTaskPo(from,jobContext,TaskStatus.Timeout,evt.at,None,evt)
    // 任务超时停止
    case evt @ TaskEvent.TimeOutStopped(jobContext)=>
      val from = sender().toString()
      insertTaskPo(from,jobContext,TaskStatus.TimeoutStopped,evt.at,None,evt)
    // 任务成功停止
    case evt @ TaskEvent.Succeed(jobContext) =>
      val from = sender().toString()
      insertTaskPo(from,jobContext,TaskStatus.Success,evt.at,None,evt)
      dataAccessProxy ! DatabaseCommand.UpdateField(DataTables.SCHEDULE,jobContext.schedule.uid,Array("SUCCEED"),self,evt)
    // 任务失败
    case evt @ TaskEvent.Failed(reason,jobContext) =>
      val from = sender().toString()
      insertTaskPo(from,jobContext,TaskStatus.Failed,evt.at,Some(reason),evt)
    // 任务超过重试次数停止
    case evt @ TaskEvent.MaxRetryReached(jobContext)=>
      val from = sender().toString()
      insertTaskPo(from,jobContext,TaskStatus.MaxRetryStopped,evt.at,None,evt)
    // 任务过载
    case  evt @ TaskActorEvent.Overloaded(jobContext) =>
      // 该节点资源不足，无法启动task，重新调度
      val from = sender().toString()
      insertTaskPo(from,jobContext,TaskStatus.Overloaded,evt.at,Some("overloaded rescheduled"),evt)

      dataAccessProxy ! DatabaseCommand.UpdateField(DataTables.SCHEDULE,jobContext.schedule.uid,Array("SUCCEED","FALSE"),self,evt)

      //scheduleAccess.setDispatched(jobContext.schedule.id,dispatched = false)
    case cmd @ TaskCommand.CheckDependency(jobId,dataTime,replyTo) =>
      // 选择是否有不成功的依赖
      dataAccessProxy ! DatabaseCommand.Select((DataTables.DEPENDENCY,jobId,dataTime),self,cmd)
    case DatabaseEvent.Selected(Some((DataTables.DEPENDENCY,state:Boolean)),TaskCommand.CheckDependency(_,_,replyTo)) =>
      replyTo ! TaskEvent.DependencyState(state)
    case Status.Failure(
    DataAccessProxyException(DatabaseCommand.Select((DataTables.DEPENDENCY,_:Long,_:Long),_,TaskCommand.CheckDependency(_,_,replyTo)),exception)) =>
      log.error(exception,exception.getMessage)
      replyTo ! TaskEvent.DependencyState(false)

    case DatabaseEvent.FieldUpdated(DataTables.SCHEDULE,Array("SUCCEED"),_:Int,Some(scheduleUid:UID),_) =>
      log.debug(s"schedule [$scheduleUid] state update true field succeed")
    case Status.Failure(DataAccessProxyException(DatabaseCommand.UpdateField(DataTables.SCHEDULE,scheduleUid:UID,Array("SUCCEED"),_,_),exception)) =>
      log.error(s"schedule [$scheduleUid] state update true field failed,reason: $exception")
    case DatabaseEvent.FieldUpdated(DataTables.SCHEDULE,Array("SUCCEED","FALSE"),_,Some(scheduleUid:UID),_) =>
      log.debug(s"schedule [$scheduleUid] state update false succeed")
    case Status.Failure(DataAccessProxyException(DatabaseCommand.UpdateField(DataTables.SCHEDULE,scheduleUid:UID,Array("SUCCEED","FALSE"),_,_),exception)) =>
      log.error(s"schedule [$scheduleUid state update false failed,reason: $exception")

    case DatabaseEvent.Inserted(Some(insertedTaskPo:TaskPo),originEvent) =>
      log.debug(s"TaskPo [${insertedTaskPo.uid}] inserted for $originEvent")
    case Status.Failure(DataAccessProxyException(DatabaseCommand.Insert(_:TaskPo,_,originEvent),exception)) =>
      log.error(exception,exception.getMessage)
      log.error(s"TaskPo insert failed for $originEvent,reason: $exception")
  }
}
