package com.gabry.job.db.proxy.actor
import akka.actor.{Props, Status}
import akka.pattern.pipe
import com.gabry.job.core.Message
import com.gabry.job.core.actor.AutoSpeedActor
import com.gabry.job.core.po.JobPo
import com.gabry.job.db.DataTables
import com.gabry.job.db.access.JobAccess
import com.gabry.job.db.proxy.DataAccessProxyException
import com.gabry.job.db.proxy.command.DatabaseCommand
import com.gabry.job.db.proxy.event.DatabaseEvent
import com.gabry.job.utils.ExternalClassHelper._
import com.typesafe.config.Config

import scala.concurrent.duration.Duration
import scala.util.{Failure, Success}
/**
  * Created by gabry on 2018/5/14 11:03
  */
object JobAccessProxy{
  def props(jobAccess:JobAccess,config:Config):Props = {
    val batchNumber = config.getIntOr("db.batch.number",1000)
    val batchInterval = config.getDurationOr("db.batch.interval",java.time.Duration.ofSeconds(3))
    Props(new JobAccessProxy(jobAccess,batchNumber,batchInterval,System.currentTimeMillis()))
  }
}
class JobAccessProxy private (jobAccess:JobAccess,batchNumber:Long,batchInterval:Duration,startTime:Long) extends AutoSpeedActor(batchNumber,batchInterval,startTime){
  private var cmdBuffer = List.empty[DatabaseCommand.Insert[JobPo]]
  private val batchEnable = config.getBooleanOr("db.batch.enable",default = false)

  /**
    * 用户自定义事件处理函数
    */
  override def userDefineEventReceive: Receive = {
    case AutoSpeedActor.BatchMessage(Some(sourceMessage:DatabaseCommand.Insert[JobPo]),_ ,commit) if !commit =>
      cmdBuffer = sourceMessage :: cmdBuffer
    case AutoSpeedActor.BatchMessage(sourceMessage: Option[DatabaseCommand.Insert[JobPo]],_ ,commit) if commit =>
      sourceMessage.foreach( msg => cmdBuffer = msg :: cmdBuffer )
      val currentCmdBuffer = cmdBuffer
      log.info(s"Batch insert commit current batch size: ${currentCmdBuffer.length}")
      jobAccess.batchInsert(currentCmdBuffer.map(_.row).toArray).onComplete{
        case Success(_) =>
          currentCmdBuffer.foreach{ insert =>
            insert.replyTo ! DatabaseEvent.Inserted(Some(insert.row),insert.originEvent)
          }
        case Failure(reason) =>
          currentCmdBuffer.foreach{ insert =>
            insert.replyTo ! Status.Failure(DataAccessProxyException(insert,reason))
          }
      }
      cmdBuffer = List.empty[DatabaseCommand.Insert[JobPo]]
    case cmd @ DatabaseCommand.Insert(row:JobPo,replyTo,_) =>
      val insertJob = jobAccess.insert(row)
      insertJob.mapAll(
        insertedJobPo => DatabaseEvent.Inserted(Some(insertedJobPo),cmd.originEvent)
        ,exception => DataAccessProxyException(cmd,exception))
        .pipeTo(replyTo)(sender())

    case selectByNameCmd @ DatabaseCommand.Select(row:JobPo,replyTo,originCommand)=>
      val select = jobAccess.selectOne(row.name)
      select.mapAll(
        selectedJobPo => DatabaseEvent.Selected(selectedJobPo,originCommand)
        ,exception => DataAccessProxyException(selectByNameCmd,exception))
        .pipeTo(replyTo)(sender())

    case cmd @ DatabaseCommand.Update(oldRow:JobPo,row:JobPo,replyTo,originCommand) =>
      val update = jobAccess.update(row)
      update.mapAll(
        updatedNum => DatabaseEvent.Updated(oldRow,if(updatedNum>0) Some(row) else None,originCommand)
        ,exception => DataAccessProxyException(cmd,exception))
        .pipeTo(replyTo)(sender())
    case DatabaseCommand.Select((DataTables.JOB,nodeAnchor:String,scheduleTime:Long,frequencyInSec:Long),replyTo,originCommand) =>
      jobAccess.selectScheduleJob(nodeAnchor,scheduleTime,frequencyInSec){ jobPo =>
        replyTo ! DatabaseEvent.Selected(Some(jobPo),originCommand)
      }
    case DatabaseCommand.Select((DataTables.JOB,nodeAnchor:String),replyTo,originCommand) =>
      jobAccess.selectJobsByScheduleNode(nodeAnchor){ job =>
        replyTo ! DatabaseEvent.Selected(Some(job),originCommand)
      }
  }

  /**
    * 获取当前消息的时间戳
    *
    * @param msg 当前消息
    * @return 当前消息的时间戳
    */
  override def getMessageTimestamp(msg: Any): Long = msg match {
    case message:Message => message.at
    case _ => System.currentTimeMillis()
  }

  /**
    * 判断当前消息是否自动驾驶，
    *
    * @param msg 当前消息
    * @return true则对此类型的消息自动调整速率
    */
  override def isAutoDriveMessage(msg: Any): Boolean = msg match {
    case _:DatabaseCommand.Insert[JobPo] => batchEnable
    case _ => false
  }
}
