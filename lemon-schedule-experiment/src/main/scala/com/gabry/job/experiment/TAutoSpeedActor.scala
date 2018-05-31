package com.gabry.job.experiment

import akka.actor.{ActorSystem, Props}
import com.gabry.job.core.actor.AutoSpeedActor
import com.typesafe.config.ConfigFactory

import scala.concurrent.duration.Duration

/**
  * Created by gabry on 2018/5/15 14:20
  */
case class AutoSpeedMessage(messageTime:Long,payload:String)
object AutoSpeed{
  def props(batchNumber: Long,batchInterval: Duration,startTime:Long):Props = Props(new AutoSpeed(batchNumber,batchInterval,startTime))
}
class AutoSpeed(batchNumber: Long,batchInterval: Duration,startTime:Long) extends AutoSpeedActor(batchNumber,batchInterval,startTime){

  /**
    * 获取当前消息的时间戳
    *
    * @param msg 当前消息
    * @return 当前消息的时间戳
    */
  override def getMessageTimestamp(msg: Any): Long = {
    msg match {
      case AutoSpeedMessage(messageTime,_) =>
        messageTime
      case _ =>
        System.currentTimeMillis()
    }
  }

  override def userDefineEventReceive: Receive = {
    case AutoSpeedMessage(messageTime,payload) =>
      log.info(s"收到单条消息 $payload at $messageTime")
    case AutoSpeedActor.BatchMessage(Some(AutoSpeedMessage(messageTime,payload)),lastMessageTime,commit) =>
      log.info(s"收到批量消息 $payload at $messageTime ,commit = $commit")
    case AutoSpeedActor.BatchMessage(None,lastMessageTime,commit) =>
      log.info(s"收到批量提交消息 at $lastMessageTime ,commit = $commit")
    case any =>
      log.info(s"未知消息 any =[$any],sender=[${sender()}]")
      //throw new Exception("")
  }

  /**
    * 判断当前消息是否自动驾驶，
    *
    * @param msg
    * @return
    */
  override def isAutoDriveMessage(msg: Any): Boolean = true
}
object TAutoSpeedActor {
  def main(args: Array[String]): Unit = {
    import scala.concurrent.duration._
    val system = ActorSystem("testAutoSpeed", ConfigFactory.load())
    val batchNum = 10
    val batchInterval = 30 millisecond
    val autoSpeedActor = system.actorOf(AutoSpeed.props(batchNum,batchInterval,1526367346362L),"TestAutoSpeedActor")

    val messageTimes = Array(
      1526367346362L,1526367346362L,1526367346362L,1526367346362L,1526367346362L
      ,1526367346362L,1526367346362L,1526367346362L,1526367346362L,1526367346363L

      ,1526367346363L,1526367346363L,1526367346363L,1526367346363L,1526367346363L
      ,1526367346363L,1526367346363L,1526367346363L,1526367346363L,1526367346363L

      ,1526367346364L,1526367346365L,1526367346366L,1526367346367L,1526367346368L
      ,1526367346368L,1526367346368L,1526367346368L,1526367346369L,1526367346370L

      ,1526367346370L,1526367346371L,1526367346372L,1526367346372L,1526367346372L
      ,1526367346372L,1526367346372L,1526367346372L)

    messageTimes.foreach{ tim =>
      Thread.sleep(3)
      autoSpeedActor ! AutoSpeedMessage(tim,s"time=$tim")
    }
    Thread.sleep(300)
    messageTimes.foreach{ tim =>
      Thread.sleep(3)
      autoSpeedActor ! AutoSpeedMessage(tim+6,s"time=$tim")
    }
  }
}
