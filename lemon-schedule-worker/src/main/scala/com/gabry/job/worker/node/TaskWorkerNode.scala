package com.gabry.job.worker.node

import akka.actor.{Props, RootActorPath}
import akka.cluster.Member
import com.gabry.job.core.command.TaskWorkerCommand
import com.gabry.job.core.constant.Constants
import com.gabry.job.core.domain.{TaskClassInfo, TaskTrackerInfo}
import com.gabry.job.core.event.TaskTrackerEvent
import com.gabry.job.core.node.{ClusterNode, ClusterNodeProps}
import com.gabry.job.utils.Utils
import com.gabry.job.worker.tracker.TaskTrackerActor

import scala.collection.JavaConverters._

/**
  * Created by gabry on 2018/3/23 15:32
  */
object TaskWorkerNode extends ClusterNodeProps{
  override def props(args: Any*): Props = Props(new TaskWorkerNode)
  override def props: Props = Props(new TaskWorkerNode)
  override val daemonName = "JobTracker"
}

/**
  * 读取配置文件，加载所有的jar包，创建并管理对应的TaskTracker
  * 每个TaskWorkerNode有多个TaskTracker对应；TaskTracker与jar包一一对应；TaskActor与jar包中的某个类一一对应
  */
class TaskWorkerNode extends ClusterNode{

  override def preStart(): Unit = {
    super.preStart()
    // 配置文件中的jars配置
    val jars = config.getConfigList("task-tracker.jars").asScala
    jars.foreach{ jar =>
      val classInfo = jar.getConfigList("classInfo").asScala.map{ clasInfo =>
        val parallel = if(clasInfo.getInt("parallel")<1) Int.MaxValue else clasInfo.getInt("parallel")
        TaskClassInfo(clasInfo.getString("name"),parallel,clasInfo.getDuration("time-out").getSeconds)
      }.toArray

      val taskTrackerInfo = TaskTrackerInfo(clusterName
        ,jar.getString("group-name")
        ,jar.getString("path")
        ,classInfo)
      log.info(s"taskTrackerInfo is $taskTrackerInfo")
      // 根据jar包中每个class的配置，发送StartTaskTracker启动TaskTracker
      self ! TaskWorkerCommand.StartTaskTracker(taskTrackerInfo,self)
    }
  }

  override def postStop(): Unit = {
    super.postStop()

  }
  override def userDefineEventReceive: Receive = {
    case TaskWorkerCommand.StartTaskTracker(taskTrackerInfo,replyTo) =>

      val taskTracker = context.actorOf(Props.create(classOf[TaskTrackerActor],taskTrackerInfo)
        ,taskTrackerInfo.group)

      context.watchWith(taskTracker,TaskTrackerEvent.TaskTrackerStopped(taskTracker))

      replyTo ! TaskTrackerEvent.TaskTrackerStarted(taskTracker)

    case evt @ TaskTrackerEvent.TaskTrackerStarted(taskTracker) =>
      log.info(s"task tracker [$taskTracker] started at ${evt.at}")
    case evt @ TaskTrackerEvent.TaskTrackerStopped(taskTracker) =>
      val stopAt = System.currentTimeMillis()
      log.warning(s"task tracker [$taskTracker] alive time is ${Utils.formatAliveTime(evt.at,stopAt)}")
      // 通知调度器，有TaskTracker退出
      currentMembers.filter(_.hasRole(Constants.ROLE_SCHEDULER_NAME))
        .map(member=>context.actorSelection(RootActorPath(member.address)/ "user" / Constants.ROLE_SCHEDULER_NAME))
        .foreach( _ ! evt )
  }

  override def register(member: Member): Unit = {
    log.info(s"member register $member")
    if(member.hasRole(Constants.ROLE_SCHEDULER_NAME)){
      // 有调度节点加入的时候，将该worker下面的TaskTracker汇报给它
      log.info(s"scheduler node address = ${RootActorPath(member.address)/ "user" / Constants.ROLE_SCHEDULER_NAME}")
      val scheduler = context.actorSelection(RootActorPath(member.address)/ "user" / Constants.ROLE_SCHEDULER_NAME)
      context.children.foreach{ taskTracker =>
        scheduler ! TaskTrackerEvent.TaskTrackerStarted(taskTracker)
      }
    }
  }

  override def unRegister(member: Member): Unit = {
    log.info(s"member unRegister $member")
  }
}
