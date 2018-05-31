package com.gabry.job.experiment

import akka.actor.{Actor, ActorLogging, ActorSystem, Props}
import com.typesafe.config.ConfigFactory

/**
  * Created by gabry on 2018/5/8 15:55
  * 主要用来测试死循环、休眠n（n比较小）毫秒时对CPU的占用情况
  */
case object Loop
class LoopedActor extends Actor with ActorLogging{
  var counter = 0L
  override def aroundReceive(receive: Receive, msg: Any): Unit = {
    counter +=1
    if(counter % 1000==0){
      self ! s"counter=$counter"
    }
    super.aroundReceive(receive, msg)
  }
  override def receive: Receive = {
    case Loop =>
      Thread.sleep(0)
      log.info(s"receive loop message at ${System.currentTimeMillis()}")
      self ! Loop
    case any=>
      log.warning(s"receive any message $any")
  }
}
object LoopActor {
  def main(args: Array[String]): Unit = {
    val system = ActorSystem("testLoop", ConfigFactory.load())
    val loop = system.actorOf(Props[LoopedActor])
    loop ! Loop
  }

}
