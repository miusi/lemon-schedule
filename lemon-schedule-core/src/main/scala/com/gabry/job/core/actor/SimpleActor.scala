package com.gabry.job.core.actor

import akka.actor.{Actor, ActorLogging, ExtendedActorSystem}
import com.typesafe.config.Config

import scala.concurrent.ExecutionContext

/**
  * Created by gabry on 2018/4/3 15:04
  * 继承Actor和ActorLogging，并提供额外的通用字段
  */
trait SimpleActor extends Actor with ActorLogging{
  /**
    * Actor当前的Address
    */
  protected lazy final val selfAddress = context.system.asInstanceOf[ExtendedActorSystem].provider.getDefaultAddress
  /**
    * Actor当前的anchor。也就是带Address的path信息
    */
  protected lazy final val selfAnchor =  self.path.toStringWithAddress(selfAddress)
  /**
    * 当前system的配置
    */
  protected final val config: Config = context.system.settings.config
  /**
    * 给Future等提供ExecutionContext
    */
  protected implicit lazy val executor: ExecutionContext = context.dispatcher
  /**
    * 用户自定义事件处理函数
    */
  def userDefineEventReceive:Receive

  /**
    * 处理未处理消息
    */
  protected def unknownEventReceive:Actor.Receive = {
    case unknownMessage:Any =>
      log.error(s"receive unknown message $unknownMessage,type is ${unknownMessage.getClass}")
  }

  override def receive: Receive = userDefineEventReceive orElse unknownEventReceive
}
