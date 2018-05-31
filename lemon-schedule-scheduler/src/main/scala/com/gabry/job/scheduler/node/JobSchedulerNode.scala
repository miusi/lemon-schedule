package com.gabry.job.scheduler.node

import akka.actor.{ActorRef, Props}
import akka.cluster.Member
import com.gabry.job.core.command.JobSchedulerCommand
import com.gabry.job.core.event.TaskTrackerEvent
import com.gabry.job.core.node.{ClusterNode, ClusterNodeProps}
import com.gabry.job.db.factory.DatabaseFactory
import com.gabry.job.db.proxy.DataAccessProxy
import com.gabry.job.scheduler.actor.{JobSchedulerActor, JobTaskAggregatorActor, JobTaskDispatcherActor}

import scala.concurrent.ExecutionContextExecutor

/**
  * Created by gabry on 2018/3/29 10:27
  */
object JobSchedulerNode extends ClusterNodeProps{
  override def props(args: Any*): Props = Props(new JobSchedulerNode)
  override def props: Props = Props(new JobSchedulerNode)

  override val daemonName: String = "JobScheduler"
}

/**
  * 按照Job频率等信息，生成作业执行计划表
  * 即根据Job生成Task。此处频率不能太高，每次需要提前生成作业执行计划表。
  * 比如每10分钟，提前生成Job未来1000个Task实例，如果1000个实例跨度小于10分钟，则进一步提高实例个数
  * 也就是提前一个调度周期，生成下个周期内所有需要执行的作业。
  */
class JobSchedulerNode extends ClusterNode{
  private var schedulerActor:ActorRef = _
  private var dispatcherActor:ActorRef = _
  private var aggregatorActor:ActorRef = _
  private val dataAccessFactory = DatabaseFactory.getDataAccessFactory(config).get
  private var dataAccessProxy:ActorRef = _
  private implicit lazy val databaseIoExecutionContext: ExecutionContextExecutor = context.system.dispatchers.lookup("akka.actor.database-io-dispatcher")

  override def preStart(): Unit = {
    super.preStart()
    dataAccessFactory.init()
    dataAccessProxy = context.actorOf(DataAccessProxy.props(databaseIoExecutionContext),"dataAccessProxy")
    context.watch(dataAccessProxy)

    schedulerActor = context.actorOf(JobSchedulerActor.props(dataAccessProxy,selfAnchor),"schedulerActor")

    context.watch(schedulerActor)
    aggregatorActor = context.actorOf(JobTaskAggregatorActor.props(dataAccessProxy,selfAnchor),"aggregatorActor")

    context.watch(aggregatorActor)
    dispatcherActor = context.actorOf(JobTaskDispatcherActor.props(dataAccessProxy,selfAnchor,aggregatorActor),"dispatcherActor")

    context.watch(dispatcherActor)

  }

  override def postStop(): Unit = {
    super.postStop()
    dataAccessFactory.destroy()
    context.stop(schedulerActor)
    context.stop(dispatcherActor)
    context.stop(aggregatorActor)
    context.stop(dataAccessProxy)

  }
  override def userDefineEventReceive: Receive = {
    case cmd @ JobSchedulerCommand.ScheduleJob(job,replyTo) =>
      schedulerActor ! cmd
    case cmd @ JobSchedulerCommand.StopScheduleJob(job) =>
      schedulerActor ! cmd
    case evt @ TaskTrackerEvent.TaskTrackerStarted(taskTracker) =>
      log.info(s"TaskTracker启动 $taskTracker")
      dispatcherActor ! evt
    case evt @ TaskTrackerEvent.TaskTrackerStopped(taskTracker) =>
      log.info(s"TaskTracker停止 $taskTracker")
      dispatcherActor ! evt
  }

  override def register(member: Member): Unit = {

  }

  override def unRegister(member: Member): Unit = {

  }
}
