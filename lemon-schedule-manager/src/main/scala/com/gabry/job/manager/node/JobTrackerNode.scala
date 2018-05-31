package com.gabry.job.manager.node

import akka.actor.{ActorRef, Props, RootActorPath}
import akka.cluster.Member
import akka.routing.{ActorSelectionRoutee, RoundRobinRoutingLogic, Router}
import com.gabry.job.core.command.{JobSchedulerCommand, JobTrackerCommand}
import com.gabry.job.core.constant.Constants
import com.gabry.job.core.domain.{Dependency, UID}
import com.gabry.job.core.event.JobTrackerEvent
import com.gabry.job.core.node.{ClusterNode, ClusterNodeProps}
import com.gabry.job.core.po.DependencyPo
import com.gabry.job.db.proxy.DataAccessProxy
import com.gabry.job.manager.actor.JobTrackerActor

import scala.concurrent.ExecutionContextExecutor
/**
  * Created by gabry on 2018/3/29 10:38
  * 2018年5月2日10:06:46：拆分JobTrackerNode部分功能，避免node节点的心跳信息无法发送给集群，导致集群误以为节点down掉
  */
object JobTrackerNode extends ClusterNodeProps{
  override def props(args: Any*): Props = Props(new JobTrackerNode)
  override def props: Props =  Props(new JobTrackerNode)
  def createDependencyPo(jobId:UID, dep: Dependency):DependencyPo =
    DependencyPo(dep.uid,jobId,dep.dependJobUid,dep.timeOffset,dep.timeOffsetUnit,dep.timeOffsetUnit.toMillis(dep.timeOffset))

  override val daemonName: String = "JobTracker"
}

/**
  * 接收客户端的信息，将Job插入队列，供Scheduler进行调度
  * 同时监控Scheduler的状态，随时调整Job所属的Scheduler
  * 尽量将Job分发给Scheduler离Task近的节点
  */
class JobTrackerNode extends ClusterNode{

  private var schedulerRouter = Router(RoundRobinRoutingLogic(),Vector.empty[ActorSelectionRoutee])
  private var jobTracker:ActorRef = _
 // private val dataAccessFactory = DatabaseFactory.getDataAccessFactory(config).get
  private implicit lazy val databaseIoExecutionContext: ExecutionContextExecutor = context.system.dispatchers.lookup("akka.actor.database-io-dispatcher")
  private var databaseAccessProxy:ActorRef = _
  override def preStart(): Unit = {
    super.preStart()
    //dataAccessFactory.init()
    databaseAccessProxy = context.actorOf(Props.create(classOf[DataAccessProxy],databaseIoExecutionContext),"DataAccessProxy")

    jobTracker = context.actorOf(JobTrackerActor.props(databaseAccessProxy))
    context.watch(jobTracker)
  }

  override def postStop(): Unit = {
    super.postStop()
    context.stop(jobTracker)
    //dataAccessFactory.destroy()
  }

  override def userDefineEventReceive: Receive = {
    /**
      * 收到客户端提交Job的命令
      * 将Job插入数据库，并将插入的结果，以JobInserted事件的形式pipe给self
      */
    case originCmd @ JobTrackerCommand.SubmitJob(job,_,_) =>
      log.debug(s"Receive SubmitJob Command $originCmd")
      jobTracker ! originCmd
    case JobTrackerCommand.ScheduleJob(job,replyTo) =>
      if(schedulerRouter.routees.nonEmpty){
        schedulerRouter.route(JobSchedulerCommand.ScheduleJob(job,self),self)
        log.info(s"Send ScheduleJob command to scheduler job.id = ${job.uid}")
        // 此处将插入后更新的Job对象发送给reply
        replyTo ! JobTrackerEvent.JobSubmitted(job)
      }else{
        replyTo ! JobTrackerEvent.JobSubmitFailed("No Scheduler node found")
      }
  }
  override def register(member: Member): Unit = {
    if(member.hasRole(Constants.ROLE_SCHEDULER_NAME)){
      val scheduleNode = context.system.actorSelection(RootActorPath(member.address)/ "user" / Constants.ROLE_SCHEDULER_NAME)
      schedulerRouter = schedulerRouter.addRoutee(scheduleNode)
    }
  }

  override def unRegister(member: Member): Unit = {
    if(member.hasRole(Constants.ROLE_SCHEDULER_NAME)){
      val scheduleNode = context.system.actorSelection(RootActorPath(member.address)/ "user" / Constants.ROLE_SCHEDULER_NAME)
      schedulerRouter = schedulerRouter.removeRoutee(scheduleNode)
    }
  }
}
