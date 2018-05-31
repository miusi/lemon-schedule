package com.gabry.job.core.po

import java.util.concurrent.TimeUnit

import com.gabry.job.core.domain.UID

/**
  * Created by gabry on 2018/4/9 18:53
  */
case class JobPo(uid: UID,
                 name: String,
                 className: String,
                 metaData: String,
                 dataTimeOffset: Long,
                 dataTimeOffsetUnit: TimeUnit,
                 startTime: Long,
                 cron: String,
                 priority: Int,
                 parallel: Int,
                 retryTimes: Int,
                 workerNodes: Option[String],
                 clusterName: String,
                 groupName: String,
                 timeout: Int,
                 replaceIfExist: Boolean,
                 lastGenerateTriggerTime: Option[Long],
                 schedulerNode: Option[String],
                 scheduleFrequency: Option[Long],
                 lastScheduleTime: Option[Long],
                 updateTime: java.sql.Timestamp = null) extends Po{
  override def toString: String = s"JobPo(uid=$uid,name=$name,class=$className,meta=$metaData,dataTimeOffset=$dataTimeOffset,dataTimeOffsetUnit=$dataTimeOffsetUnit," +
    s"startTime=$startTime,cron=$cron,priority=$priority,parallel=$parallel,retryTimes=$retryTimes,workerNodes=${workerNodes.mkString(",")}," +
    s"cluster=$clusterName,group=$groupName,timeout=$timeout,replaceIfExist=$replaceIfExist,lastGenerateTriggerTime=$lastGenerateTriggerTime," +
    s"schedulerNode=$schedulerNode,scheduleFrequency=$scheduleFrequency,lastScheduleTime=$lastScheduleTime)"
}
