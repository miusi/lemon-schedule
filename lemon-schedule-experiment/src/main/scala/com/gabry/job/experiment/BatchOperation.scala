package com.gabry.job.experiment

import akka.actor.{Actor, ActorLogging, ActorSystem, Props, ReceiveTimeout}
import com.typesafe.config.ConfigFactory

import scala.collection.mutable.ArrayBuffer
import scala.concurrent.ExecutionContextExecutor
import scala.concurrent.duration._
import scala.util.Random
/**
  * Created by gabry on 2018/5/11 15:51
  */

final case class Message(msg:String,messageTime:Long = System.currentTimeMillis())
final case class CheckRatioTime(startTime:Long)
/**
  * 批量操作actor
  * @param batchNum 批量操作的个数
  * @param batchIntervalInMilliSec 批量操作的时间间隔
  */
class BatchActor(batchNum:Long, batchIntervalInMilliSec:Int) extends Actor with ActorLogging{
  private val batchInterval = batchIntervalInMilliSec millisecond
  /**
    * 用于缓存消息，如果操作数据库可以使用addBatch替换该数组
    */
  private val messageQueue: ArrayBuffer[Message] = ArrayBuffer.empty[Message]
  /**
    * 消息处理计数器
    */
  private var processCounter:Long = 0
  /**
    * 消息最后一次批量开始的时间
    */
  private var processStartTime:Long = System.currentTimeMillis()
  /**
    * 最后一条消息的处理时间
    */
  private var lastMessageTime:Long = System.currentTimeMillis()

  private implicit val executionContext: ExecutionContextExecutor = context.dispatcher
  private def processSingleMessage(message: Message):Unit = {
    log.info(s"单条数据处理完毕 $message at ${message.messageTime}")
  }
  private def addBatch(message: Message):Unit = {
    messageQueue.append(message)
  }
  private def executeBatch():Unit = {
    log.info("执行批量提交")
    messageQueue.foreach( m => log.info(s"$m at ${m.messageTime}") )
    messageQueue.clear()
    log.info("批量提交执行完毕")
  }
  private def batchProcess:Receive = {
    case evt @ Message(msg,messageTime) =>
      addBatch(evt)
      lastMessageTime = messageTime
      processCounter += 1
      if(processCounter % batchNum == 0 ){
        executeBatch()
      }
    case CheckRatioTime(startTime) =>
      log.info(s"收到了CheckRatio消息 $startTime")
      executeBatch()
      if(processCounter < batchNum){
        log.info("间隔时间内消息过少，则退出批量调度")
        processCounter = 0
        processStartTime = lastMessageTime
       // context.setReceiveTimeout(Duration.Undefined)
        context.become(singleProcess)
      }else{
        // 收到超时时间强制刷新批量数据
        processStartTime = lastMessageTime
        processCounter = 0
        context.system.scheduler.scheduleOnce(batchInterval,self,CheckRatioTime(lastMessageTime))
      }
    case timeout:ReceiveTimeout =>
      log.info(s"收到了timeout消息 $processCounter")
      executeBatch()
      val currentTime = System.currentTimeMillis()
      if(processCounter < batchNum){
        log.info("间隔时间内消息过少，则退出批量调度")
        processCounter = 0
        processStartTime = currentTime
        context.setReceiveTimeout(Duration.Undefined)
        context.become(singleProcess)
      }else{
        // 收到超时时间强制刷新批量数据
        processStartTime = currentTime
        processCounter = 0
      }
    case unkonMessage =>
      println(s"unkonMessage=$unkonMessage")
  }
  private def singleProcess:Receive = {
    case evt @ Message(msg,messageTime) =>
      // 处理单条消息
      processSingleMessage(evt)
      processCounter += 1
      lastMessageTime = messageTime
      if( processCounter == batchNum ){
        processCounter = 0
        //val currentMessageTime = messageTime
        log.info(s"到达一个批量，计算此次批量的时间跨度${lastMessageTime - processStartTime}")
        if(lastMessageTime - processStartTime >= batchIntervalInMilliSec){
          log.info("时间跨度过大，即短时间内数量不大，保持在单条模式")
          processStartTime = lastMessageTime
        }else{
          log.info("时间跨度太短，即短时间内数据量太大，进入批量模式")
          processStartTime = lastMessageTime
          //        context.setReceiveTimeout(batchIntervalInMilliSec millisecond)
          context.system.scheduler.scheduleOnce(batchInterval,self,CheckRatioTime(lastMessageTime))
          context.become(batchProcess)
        }
      }
    case unkonMessage =>
      println(s"unkonMessage=$unkonMessage")
  }

  /**
    * 刚开始设置为批量模式，提高消息的响应速度
    */
  override def receive: Receive = singleProcess
}
object BatchOperation {
  def main(args: Array[String]): Unit = {
    val system = ActorSystem("testLoop", ConfigFactory.load())
    val batchNum = 10
    val batchInterval = 3
    val batch = system.actorOf(Props(new BatchActor(batchNum,batchInterval)),"BatchActor")

    0 until batchNum foreach { i =>
      batch ! Message(s"0-test-$i")
    }
    Thread.sleep(batchInterval*3)
    0 until batchNum foreach { i =>
      batch ! Message(s"1-test-$i")
      val time = Random.nextInt(6)
      Thread.sleep(time)
    }
    0 until batchNum foreach { i =>
      batch ! Message(s"2-test-$i")
    }

  }
}
