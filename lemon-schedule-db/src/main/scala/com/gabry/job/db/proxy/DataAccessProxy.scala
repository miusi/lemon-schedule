package com.gabry.job.db.proxy

import akka.actor.{ActorRef, Props}
import com.gabry.job.core.actor.SimpleActor
import com.gabry.job.core.domain.UID
import com.gabry.job.core.po.{DependencyPo, JobPo, SchedulePo, TaskPo}
import com.gabry.job.db.DataTables
import com.gabry.job.db.factory.DatabaseFactory
import com.gabry.job.db.proxy.actor.{DependencyAccessProxy, JobAccessProxy, ScheduleAccessProxy, TaskAccessProxy}
import com.gabry.job.db.proxy.command.DatabaseCommand

import scala.concurrent.ExecutionContextExecutor

/**
  * Created by gabry on 2018/5/14 10:30
  * DataAccessProxy负责对所有操作数据库的消息进行路由
  */
object DataAccessProxy{
  def props(databaseIoExecutionContext: ExecutionContextExecutor):Props = Props(new DataAccessProxy(databaseIoExecutionContext))
}
class DataAccessProxy private (databaseIoExecutionContext: ExecutionContextExecutor) extends SimpleActor{
  private implicit val databaseExecutionContext:ExecutionContextExecutor = databaseIoExecutionContext
  private val dataAccessFactory = DatabaseFactory.getDataAccessFactory(config).get
  private var jobAccessProxy:ActorRef = _
  private var scheduleAccessProxy: ActorRef = _
  private var dependencyAccessProxy:ActorRef = _
  private var taskAccessProxy: ActorRef = _
  override def preStart(): Unit = {
    super.preStart()
    dataAccessFactory.init()

    jobAccessProxy = context.actorOf(JobAccessProxy.props(dataAccessFactory.getJobAccess,config),"JobAccessProxy")
    scheduleAccessProxy = context.actorOf(Props.create(classOf[ScheduleAccessProxy],dataAccessFactory.getScheduleAccess),"ScheduleAccessProxy")
    dependencyAccessProxy = context.actorOf(Props.create(classOf[DependencyAccessProxy],dataAccessFactory.getDependencyAccess),"DependencyAccessProxy")

    taskAccessProxy = context.actorOf(TaskAccessProxy.props(dataAccessFactory.getTaskAccess,config),"TaskAccessProxy")
  }
  override def postStop(): Unit = {
    super.preStart()
    dataAccessFactory.destroy()
  }

  /**
    * 用户自定义事件处理函数
    */
  override def userDefineEventReceive: Receive = {
    case cmd @ DatabaseCommand.Insert(_:JobPo,_,_) =>
      jobAccessProxy ! cmd
    case cmd @ DatabaseCommand.Insert(_:SchedulePo,_,_) =>
      scheduleAccessProxy ! cmd
    case cmd @ DatabaseCommand.Insert(_:Array[DependencyPo],_,_) =>
      dependencyAccessProxy ! cmd
    case cmd @ DatabaseCommand.Insert(_:TaskPo,_,_) =>
      taskAccessProxy ! cmd
    case cmd @ DatabaseCommand.Select(_:JobPo,_,_) =>
      jobAccessProxy ! cmd
    case cmd @ DatabaseCommand.Update(_:JobPo,_:JobPo,_,_) =>
      jobAccessProxy ! cmd
    case cmd @ DatabaseCommand.UpdateField(DataTables.SCHEDULE,_,_,_,_) =>
      scheduleAccessProxy ! cmd
    case cmd @ DatabaseCommand.Select((DataTables.DEPENDENCY,_,_),_,_) =>
      dependencyAccessProxy ! cmd
    case cmd @ DatabaseCommand.Select((DataTables.JOB,_,_,_),_,_) =>
      jobAccessProxy ! cmd
    case cmd @ DatabaseCommand.Select((DataTables.SCHEDULE,jobUid:UID,nodeAnchor:String,triggerTime:Long,parallel:Int),_,_) =>
      scheduleAccessProxy ! cmd
    case cmd @ DatabaseCommand.Select((DataTables.JOB,nodeAnchor:String),_,_) =>
      jobAccessProxy ! cmd
  }
}