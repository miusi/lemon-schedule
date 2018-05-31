package com.gabry.job.core

import java.util.concurrent.TimeUnit

import com.gabry.job.core.domain.{Dependency, Job}

import scala.util.parsing.json.JSON

/**
  * Created by gabry on 2018/4/24 17:29
  */
package object po {

  implicit def job2JobPo(job:Job):JobPo =
    JobPo(job.uid,job.name,job.className,job.getMetaJsonString(),job.dataTimeOffset,job.dataTimeOffsetUnit,job.startTime,job.cron
      ,job.priority,job.parallel,job.retryTimes,Some(job.workerNodes.mkString(",")),job.cluster,job.group,job.timeOut,job.replaceIfExist
      ,None,None,None,None,updateTime =  null)

  implicit def jobPo2Job(row:JobPo):Job =
    Job(row.uid,row.name,row.className,row.cron,row.dataTimeOffset
    ,row.dataTimeOffsetUnit, row.parallel
    ,JSON.parseFull(row.metaData).get.asInstanceOf[Map[String,String]]
    ,row.workerNodes.map(_.split(",")).getOrElse(Array.empty[String]),row.clusterName
    ,row.groupName,row.startTime,row.priority,row.retryTimes
    ,row.timeout,row.replaceIfExist)

  implicit def dependencyPo2Dependency(row: DependencyPo):Dependency =
    Dependency(row.uid,row.jobUid,row.dependJobUid,row.timeOffset,TimeUnit.valueOf(row.timeOffsetUnit))

  implicit def dependency2DependencyPo(dep: Dependency):DependencyPo =
    DependencyPo(dep.uid,dep.jobUid,dep.dependJobUid,dep.timeOffset,dep.timeOffsetUnit.toString,dep.timeOffsetUnit.toMillis(dep.timeOffset))

//  implicit def schedule2SchedulePo(schedule:Schedule):SchedulePo =
//    SchedulePo(schedule.id,schedule.jobUid,schedule.priority,schedule.retryTimes,schedule.dispatched,schedule.triggerTime,schedule.scheduleNode,schedule.scheduleTime,succeed = false,System.currentTimeMillis())

  implicit def timeUnit2String(timeUnit:TimeUnit):String = timeUnit.toString

  implicit def string2TimeUnit(timeUnitName:String):TimeUnit = TimeUnit.valueOf(timeUnitName)
}
