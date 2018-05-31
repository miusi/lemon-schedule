package com.gabry.job.db.proxy.actor
import akka.pattern.pipe
import com.gabry.job.core.actor.SimpleActor
import com.gabry.job.core.domain.UID
import com.gabry.job.core.po.SchedulePo
import com.gabry.job.db.DataTables
import com.gabry.job.db.access.ScheduleAccess
import com.gabry.job.db.proxy.DataAccessProxyException
import com.gabry.job.db.proxy.command.DatabaseCommand
import com.gabry.job.db.proxy.event.DatabaseEvent
import com.gabry.job.utils.ExternalClassHelper._
/**
  * Created by gabry on 2018/5/14 11:12
  */
class ScheduleAccessProxy(scheduleAccess:ScheduleAccess) extends SimpleActor{
  /**
    * 用户自定义事件处理函数
    */
  override def userDefineEventReceive: Receive = {
    case cmd @ DatabaseCommand.Insert(schedulePo:SchedulePo,replyTo,originEvent) =>
      scheduleAccess.insert(schedulePo).mapAll(
        insertedSchedulePo => DatabaseEvent.Inserted(Some(insertedSchedulePo),originEvent),
        exception => DataAccessProxyException(cmd,exception))
        .pipeTo(replyTo)(sender())

    case cmd @ DatabaseCommand.UpdateField(DataTables.SCHEDULE,scheduleUid:UID,fields @ Array("DISPATCH"),replyTo,originEvent) =>
      scheduleAccess.setDispatched(scheduleUid,dispatched = true).mapAll(
        updateNum =>DatabaseEvent.FieldUpdated(DataTables.SCHEDULE,fields,updateNum,Some(scheduleUid),originEvent),
        exception =>DataAccessProxyException(cmd,exception))
        .pipeTo(replyTo)(sender())

    case cmd @ DatabaseCommand.UpdateField(DataTables.SCHEDULE,scheduleUid,fields @ Array("SUCCEED"),replyTo,originEvent) =>
      scheduleAccess.setSucceed(scheduleUid,succeed = true).mapAll(
        updateNum => DatabaseEvent.FieldUpdated(DataTables.SCHEDULE,fields,updateNum,Some(scheduleUid),originEvent),
        exception => DataAccessProxyException(cmd,exception))
        .pipeTo(replyTo)(sender())
    case cmd @ DatabaseCommand.UpdateField(DataTables.SCHEDULE,scheduleUid,fields @ Array("SUCCEED","FALSE"),replyTo,originEvent) =>
      scheduleAccess.setSucceed(scheduleUid,succeed = false).mapAll(
        updateNum => DatabaseEvent.FieldUpdated(DataTables.SCHEDULE,fields,updateNum,Some(scheduleUid),originEvent),
        exception =>DataAccessProxyException(cmd,exception))
        .pipeTo(replyTo)(sender())
    case DatabaseCommand.Select((DataTables.SCHEDULE,jobUid:UID,nodeAnchor:String,triggerTime:Long,jobParallel:Int),replyTo,originCommand) =>
      scheduleAccess.selectUnDispatchSchedule(jobUid,nodeAnchor,triggerTime,jobParallel){ schedulePo =>
        replyTo ! DatabaseEvent.Selected(Some(schedulePo),originCommand)
      }
  }
}
