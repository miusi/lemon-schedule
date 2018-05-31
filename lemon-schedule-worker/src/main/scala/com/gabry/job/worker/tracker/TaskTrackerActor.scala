package com.gabry.job.worker.tracker

import akka.actor.Props
import com.gabry.job.core.actor.SimpleActor
import com.gabry.job.core.command.{TaskActorCommand, TaskTrackerCommand}
import com.gabry.job.core.domain._
import com.gabry.job.core.event.TaskActorEvent
import com.gabry.job.core.task.Task
import com.gabry.job.utils.{TaskClassLoader, Utils}

import scala.util.{Failure, Success}

/**
  * Created by gabry on 2018/3/23 16:54
  */
/**
  * TaskTracker类，加载指定的jar包，并创建对应的TaskActor类
  * 一个TaskTracker对应多个TaskActor；TaskActor与其中的Task类一一对应
  * 用来封装jar包，执行jar包中实现Task接口的类
  * @param taskTrackerInfo TaskTracker参数信息
  */
class TaskTrackerActor(taskTrackerInfo:TaskTrackerInfo) extends SimpleActor{
  /**
    * TaskTracker负责JAR包的类加载器
    */
  private val taskClassLoader = new TaskClassLoader(taskTrackerInfo.jarPath)
  // 根据actor的生命周期按需加载jar包中的Task类，包括重启、卸载等
  override def preStart(): Unit = {
    super.preStart()
    taskClassLoader.init()
    taskClassLoader.getUrls.foreach{ url =>
      log.warning(s"loaded url [$url]")
    }
    // 加载配中的各个class
    taskTrackerInfo.classInfo.foreach{ info =>
      taskClassLoader.load(info.name) match {
        case Success(claz) if classOf[Task].isAssignableFrom(claz) =>
          // 成功加载后发送StartTaskActor命令
          self ! TaskTrackerCommand.StartTaskActor(info,claz,self)
        case Success(claz) =>
          log.error(s"class $claz found but not a Task")
        case Failure(reason) =>
          log.error(reason,reason.getMessage)
      }
    }
  }

  override def postStop(): Unit = {
    super.postStop()
    taskClassLoader.destroy()
  }
  override def userDefineEventReceive: Receive = {
    case runCmd @ TaskActorCommand.RunTask( jobContext,replyTo ) =>
      context.child(jobContext.job.className).foreach(_ ! runCmd)

    case TaskTrackerCommand.StartTaskActor(info,claz,replyTo)=>

      val taskActorInfo = TaskActorInfo(taskTrackerInfo.cluster,taskTrackerInfo.group,claz,info)

      val taskActor = context.actorOf(Props.create(classOf[TaskActor],taskActorInfo),info.name)

      // TaskActor负责某一个类的初始化、执行等操作，启动成功后告诉汇报者TaskActorEvent.Started消息
      replyTo ! TaskActorEvent.Started(taskActor,info)
      context.watchWith(taskActor,TaskActorEvent.Stopped(taskActor))

    case evt @ TaskActorEvent.Started(taskActor,taskClassInfo)=>
      log.info(s"task actor [$taskActor] start at ${evt.at},classInfo is $taskClassInfo")
    case evt @ TaskActorEvent.Stopped(taskActor) =>
      val stopAt = System.currentTimeMillis()
      log.warning(s"task actor [$taskActor] alive time is ${Utils.formatAliveTime(evt.at,stopAt)}")

    case unKnownMessage =>
      log.error(s"unKnownMessage $unKnownMessage")
  }
}
