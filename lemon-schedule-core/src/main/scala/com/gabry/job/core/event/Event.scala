package com.gabry.job.core.event

import java.nio.file.WatchEvent

import akka.actor.ActorRef
import com.gabry.job.core.Message
import com.gabry.job.core.domain.{Job, JobContext, TaskClassInfo}

/**
  * Created by gabry on 2018/3/23 15:56
  * 事件（event）的接口，标志发生了某件事
  */
// TODO: 注意所有的事件case class都需要简化字段，去掉不必需的字段，以节省网络带宽。前期阶段可先不考虑该问题
trait Event extends Message

trait FailedEvent extends Event{
  def reason:String
}
/**
  * TaskWorker需要处理的消息
  */

object TaskWorkerEvent{

  /**
    * LIB文件变化消息
    * @param watchEvent 此次变化的具体信息
    */
  final case class LibFileChanged(watchEvent: WatchEvent[_]) extends Event

  /**
    * 通知监听者，lib文件没有变化。考虑用来获取文件目录没有变化的信息
    */
  final case class NoLibFileChanged() extends Event
}

object TaskTrackerEvent{
  final case class TaskTrackerStarted(actor:ActorRef) extends Event
  final case class TaskTrackerStopped(actor:ActorRef) extends Event
}

object TaskActorEvent{
  final case class Started(actor:ActorRef,taskClassInfo: TaskClassInfo ) extends Event
  final case class Cancelled(jobContext: JobContext) extends Event
  final case class Stopped(actor:ActorRef) extends Event
  final case class Overloaded(jobContext: JobContext) extends Event
}

trait JobContextEvent {
  def jobContext:JobContext
}
object TaskRunnerEvent {
  final case class Stopped(actor:ActorRef) extends Event
}
object TaskEvent{

  /**
    * Task执行失败的事件
    * @param reason 执行失败的原因
    */
  final case class Failed(reason: String, jobContext:JobContext) extends FailedEvent with JobContextEvent
  final case class Succeed(jobContext:JobContext) extends Event with JobContextEvent
  final case class Terminated(jobContext:JobContext) extends Event with JobContextEvent
  final case class Started( actor:ActorRef,jobContext:JobContext) extends Event with JobContextEvent
  final case class Waiting(jobContext:JobContext) extends Event with JobContextEvent
  final case class Timeout(jobContext:JobContext) extends Event with JobContextEvent
  //final case class Cancelled(replyTo:ActorRef,jobContext:JobContext) extends Event with JobContextEvent
  final case class Executing(jobContext:JobContext) extends Event with JobContextEvent
  final case class MaxRetryReached(jobContext:JobContext) extends Event with JobContextEvent
  final case class TimeOutStopped(jobContext:JobContext) extends Event with JobContextEvent
  final case class DependencyState(passed:Boolean) extends Event
}

object JobTrackerEvent{
  final case class JobSubmitted(submittedJob:Job) extends Event
  final case class JobSubmitFailed(reason: String) extends FailedEvent
}

object JobSchedulerEvent{
  final case class Failure(job:Job, reason: String) extends FailedEvent
}

/**
  * 作业执行时的异常事件
  */
object TaskExceptionEvent{
  final case class Cancelled(reason:String) extends FailedEvent
  final case class TimeOut(timeout:Long, reason:String) extends FailedEvent
}
object JobClientEvent{
  final case class Failure(reason: String ) extends FailedEvent
  final case class ManagerJoined(anchor:String) extends Event
  final case class ManagerLeaved(anchor:String) extends Event
}