package com.gabry.job.client.akkaclient

import akka.actor.ActorRef
import akka.routing.{ActorRefRoutee, ActorSelectionRoutee, RoundRobinRoutingLogic, Router}
import com.gabry.job.core.actor.SimpleActor
import com.gabry.job.core.command.{JobClientCommand, JobTrackerCommand}
import com.gabry.job.core.constant.Constants
import com.gabry.job.core.domain.Node
import com.gabry.job.core.event.JobClientEvent
import com.gabry.job.core.registry.RegistryEvent.RegistryEvent
import com.gabry.job.core.registry.{Registry, RegistryEvent, RegistryListener}

/**
  * Created by gabry on 2018/4/4 10:21
  * 客户端背后的actor
  */
class ClientActor(registry:Registry) extends SimpleActor{
  private var jobTrackerRouter = Router(RoundRobinRoutingLogic(),Vector.empty[ActorRefRoutee])
  private val registryListener = new MyRegistryListener(self)
  override def preStart(): Unit = {
    super.preStart()
    registry.subscribe(registryListener)
    val jobTrackers = registry.getNodesByType(Constants.ROLE_MANAGER_NAME).map(node =>ActorSelectionRoutee(context.actorSelection(node.anchor)))
    log.info(s"jobTrackers=${jobTrackers.mkString(",")}")
    jobTrackerRouter = jobTrackerRouter.withRoutees(jobTrackers.toIndexedSeq)
  }

  override def postStop(): Unit = {
    super.postStop()
    registry.unSubscribe(registryListener)
  }
  override def userDefineEventReceive: Receive = {
    case JobClientEvent.ManagerJoined(anchor) =>
      jobTrackerRouter = jobTrackerRouter.addRoutee(ActorSelectionRoutee(context.actorSelection(anchor)))
    case JobClientEvent.ManagerLeaved(anchor)=>
      jobTrackerRouter = jobTrackerRouter.removeRoutee(ActorSelectionRoutee(context.actorSelection(anchor)))
    case JobClientCommand.Start =>
      log.info(s"ClientActor start at $selfAddress")
    case JobClientCommand.Stop =>
      log.info(s"ClientActor stop from $selfAddress")
    case JobClientCommand.SubmitJob(job,dependency) =>
      val from = sender()
      if(jobTrackerRouter.routees.nonEmpty){
        // 由路由到的jobTracker回复此次submit命令
        jobTrackerRouter.route(JobTrackerCommand.SubmitJob(job,dependency,from),from)
      }else {
        from ! JobClientEvent.Failure("No Job Tracker Found")
      }
    case unKnownMessage =>
      log.error(s"unKnownMessage $unKnownMessage")
  }
}
private[akkaclient] class MyRegistryListener(replyTo:ActorRef) extends RegistryListener{
  /**
    * 检测当前节点的事件是否满足监听条件
    * 满足监听条件的会调用onEvent函数
    * @param node 事件对应的节点
    * @param event 发生的事件
    * @return true满足监听条件
    */
  override def filter(node: Node, event: RegistryEvent): Boolean = node.nodeType == Constants.ROLE_MANAGER_NAME

  /**
    * 事件回调函数
    *
    * @param node  对应的节点
    * @param event 发生的事件
    */
  override def onEvent(node: Node, event: RegistryEvent): Unit = {
    event match {
      case RegistryEvent.JOIN =>
        replyTo ! JobClientEvent.ManagerJoined(node.anchor)
      case RegistryEvent.LEAVE =>
        replyTo ! JobClientEvent.ManagerLeaved(node.anchor)
    }
  }
}