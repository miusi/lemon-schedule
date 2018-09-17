package com.gabry.job.experiment

import akka.actor.{Actor, ActorCell, ActorSystem, Props}
import com.typesafe.config.ConfigFactory

/**
  * Created by gabry on 2018/7/24 14:21
  */
class ActorContextPrintActor extends Actor{
  throw new Exception("")
  var hascode = context.hashCode()
  var threadId = Thread.currentThread().getId
  println(context.hashCode())
  println(threadId)
  override def receive: Receive = {
    case "print" =>
      if(threadId!=Thread.currentThread().getId){
        println(s"threadid = $threadId,currentId = ${Thread.currentThread().getId},hashcode=$hascode,contextCode=${context.hashCode()}")
      }
  }
}
object ActorContextPrint {
  def main(args: Array[String]): Unit = {
    val system = ActorSystem("ActorContext",ConfigFactory.load())
    val actor1 = system.actorOf(Props(new ActorContextPrintActor),"actor1")
    val number = 10000
    1 to number foreach{ i =>
    //  actor1 ! "print"
    }
  }
}
