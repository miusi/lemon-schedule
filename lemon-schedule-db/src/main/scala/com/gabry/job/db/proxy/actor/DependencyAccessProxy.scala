package com.gabry.job.db.proxy.actor

import akka.pattern.pipe
import com.gabry.job.core.actor.SimpleActor
import com.gabry.job.core.command.JobTrackerCommand
import com.gabry.job.core.domain.UID
import com.gabry.job.core.po.DependencyPo
import com.gabry.job.db.DataTables
import com.gabry.job.db.access.DependencyAccess
import com.gabry.job.db.proxy._
import com.gabry.job.db.proxy.command.DatabaseCommand
import com.gabry.job.db.proxy.event.DatabaseEvent
import com.gabry.job.utils.ExternalClassHelper._
/**
  * Created by gabry on 2018/5/14 11:13
  */
class DependencyAccessProxy(dependencyAccess:DependencyAccess) extends SimpleActor{
  /**
    * 用户自定义事件处理函数
    */
  override def userDefineEventReceive: Receive = {
    case cmd @ DatabaseCommand.Insert(row:Array[DependencyPo],replyTo,originCommand:JobTrackerCommand.SubmitJob) =>

      val insertDependency = dependencyAccess.deleteAllAndInsertMany(originCommand.job.uid,row)
      insertDependency.mapAll( insertedNum =>
        DatabaseEvent.BatchInserted(insertedNum.getOrElse(0),row.headOption,originCommand)
        ,exception => DataAccessProxyException(cmd,exception))
        .pipeTo(replyTo)(sender())
    case cmd @ DatabaseCommand.Select((DataTables.DEPENDENCY,jobId:UID,dataTime:Long),replyTo,originCommand) =>
      dependencyAccess.selectSucceedState(jobId,dataTime).mapAll(
        succeed => DatabaseEvent.Selected(Some((DataTables.DEPENDENCY,succeed)),originCommand),
        exception => DataAccessProxyException(cmd,exception) )
        .pipeTo(replyTo)(sender())
  }
}
