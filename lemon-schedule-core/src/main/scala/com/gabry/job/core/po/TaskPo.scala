package com.gabry.job.core.po

import com.gabry.job.core.domain.TaskStatus.TaskStatus
import com.gabry.job.core.domain.UID

/**
  * Created by gabry on 2018/4/24 16:14
  */
case class TaskPo(uid: UID,
                  jobUid: UID,
                  scheduleUidd: UID,
                  retryId: Int,
                  taskTrackerNode: String,
                  state: TaskStatus,
                  eventTime: Long,
                  message: Option[String],
                  updateTime: java.sql.Timestamp = null) extends Po{
  override def toString: String = s"TaskPo(uid=$uid,jobUid=$jobUid,scheduleUidd=$scheduleUidd,retryId=$retryId,taskTrackerNode=$taskTrackerNode," +
    s"state=$state,eventTime=$eventTime,message=$message)"
}