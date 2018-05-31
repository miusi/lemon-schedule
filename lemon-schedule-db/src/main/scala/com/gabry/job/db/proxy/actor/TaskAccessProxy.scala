package com.gabry.job.db.proxy.actor

import akka.actor.{Props, Status}
import akka.pattern.pipe
import com.gabry.job.core.Message
import com.gabry.job.core.actor.AutoSpeedActor
import com.gabry.job.core.po.TaskPo
import com.gabry.job.db.access.TaskAccess
import com.gabry.job.db.proxy.DataAccessProxyException
import com.gabry.job.db.proxy.command.DatabaseCommand
import com.gabry.job.db.proxy.event.DatabaseEvent
import com.gabry.job.utils.ExternalClassHelper._
import com.typesafe.config.Config

import scala.concurrent.duration._
import scala.util.{Failure, Success}

/**
  * Created by gabry on 2018/5/14 11:09
  */
object TaskAccessProxy{
  def props(taskAccess:TaskAccess,config:Config):Props = {
    val batchNumber = config.getIntOr("db.batch.number",1000)
    val batchInterval = config.getDurationOr("db.batch.interval",java.time.Duration.ofSeconds(3))
    Props(new TaskAccessProxy(taskAccess,batchNumber,batchInterval,System.currentTimeMillis()))
  }
}
class TaskAccessProxy private (taskAccess:TaskAccess,batchNumber:Long,batchInterval:Duration,startTime:Long) extends AutoSpeedActor(batchNumber ,batchInterval ,startTime){
  private var cmdBuffer = List.empty[DatabaseCommand.Insert[TaskPo]]
  private val batchEnable = config.getBooleanOr("db.batch.enable",default = false)
  /**
    * 用户自定义事件处理函数
    */
  override def userDefineEventReceive: Receive = {
    case cmd @ DatabaseCommand.Insert(taskPo:TaskPo,replyTo,originEvent) =>
      taskAccess.insert(taskPo).mapAll(
        insertedTaskPo => DatabaseEvent.Inserted(Some(insertedTaskPo),originEvent),
        exception => DataAccessProxyException(cmd,exception))
        .pipeTo(replyTo)(sender())
    case AutoSpeedActor.BatchMessage(Some(sourceMessage : DatabaseCommand.Insert[TaskPo]),_,commit) if !commit =>
      cmdBuffer = sourceMessage :: cmdBuffer
    case AutoSpeedActor.BatchMessage(sourceMessage:Option[DatabaseCommand.Insert[TaskPo]],_,commit) if commit =>
      sourceMessage match {
        case Some(msg) =>
          cmdBuffer = msg :: cmdBuffer
        case None =>
      }
      // 批量通知
      val currentCmdBuffer = cmdBuffer
      taskAccess.batchInsert(currentCmdBuffer.map(_.row).toArray).onComplete{
        case Success(_) =>
          currentCmdBuffer.foreach{ insert =>
            insert.replyTo ! DatabaseEvent.Inserted(Some(insert.row),insert.originEvent)
          }
        case Failure(reason) =>
          currentCmdBuffer.foreach{ insert =>
            insert.replyTo ! Status.Failure(DataAccessProxyException(insert,reason))
          }
      }
      cmdBuffer = List.empty[DatabaseCommand.Insert[TaskPo]]
  }

  /**
    * 获取当前消息的时间戳
    *
    * @param msg 当前消息
    * @return 当前消息的时间戳
    */
  override def getMessageTimestamp(msg: Any): Long = msg match {
    case message: Message => message.at
    case _ => System.currentTimeMillis()
  }

  /**
    * 判断当前消息是否自动驾驶，
    *
    * @param msg 当前消息
    * @return true则对此类型的消息自动调整速率
    */
  override def isAutoDriveMessage(msg: Any): Boolean = batchEnable
}
