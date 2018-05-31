package com.gabry.job.db.slicks

import com.gabry.job.core.po._
import com.gabry.job.db.access.JobAccess
import com.gabry.job.db.slicks.schema.Tables
import slick.jdbc.MySQLProfile.api._

import scala.concurrent.{ExecutionContext, Future}
/**
  * Created by gabry on 2018/3/29 15:16
  */
private[db] class SlickJobAccess(db:Database) extends SlickDataAccess(db) with JobAccess {
  private lazy val tables = Tables.Jobs

  private def insertForUpdate(r: Tables.JobsRow):DBIO[Int] =
    sqlu"""INSERT INTO jobs(`name`,class_name,meta_data,data_time_offset,data_time_offset_unit,start_time,
          cron,priority,retry_times,cluster_name,group_name,timeout,
          replace_if_exist,last_generate_trigger_time,
          scheduler_node,schedule_frequency,last_schedule_time,update_time)
          VALUES(${r.name},${r.className},${r.metaData},${r.dataTimeOffset},${r.dataTimeOffsetUnit},${r.startTime},
          ${r.cron},${r.priority},${r.retryTimes},${r.clusterName},${r.groupName},${r.timeout},
          ${r.replaceIfExist},${r.lastGenerateTriggerTime},
          ${r.schedulerNode},${r.scheduleFrequency},${r.lastScheduleTime},null)
          on duplicate key update class_name=VALUES(class_name),
                                   meta_data=VALUES(meta_data),
                                   data_time_offset = VALUES(data_time_offset),
                                   data_time_offset_unit = VALUES(data_time_offset_unit),
                                   start_time=VALUES(start_time),
                                   cron=VALUES(cron),
                                   priority=VALUES(priority),
                                   retry_times=VALUES(retry_times),
                                   cluster_name=VALUES(cluster_name),
                                   group_name=VALUES(group_name),
                                   timeout=VALUES(timeout),
                                   replace_if_exist=VALUES(replace_if_exist),
                                   last_generate_trigger_time=VALUES(last_generate_trigger_time),
                                   scheduler_node=VALUES(scheduler_node),
                                   schedule_frequency=VALUES(schedule_frequency),
                                   last_schedule_time=VALUES(last_schedule_time),
                                   update_time=null"""

  override def insert(job: JobPo)(implicit global: ExecutionContext): Future[JobPo] = {
   val insertAction = (tables
     returning
     tables.map(_.rowId)) into ((job,newId)=>job.copy(rowId=newId)) += job
    db.run(insertAction).map(r=>jobRow2bPo(r))
  }

  override def delete(job: JobPo)(implicit global: ExecutionContext): Future[Int] = {
    val deleteAction = tables.filter(_.name === job.name).delete
    db.run(deleteAction)
  }

  override def update(job: JobPo)(implicit global: ExecutionContext): Future[Int] = {
    val updateAction = tables.filter(_.name === job.name).map(r=>(r.className,
      r.metaData,r.dataTimeOffset,r.dataTimeOffsetUnit,
      r.startTime,r.cron,r.priority,r.parallel,r.retryTimes,
      r.workerNodes,r.clusterName,r.groupName,r.timeout,r.replaceIfExist,
      r.lastGenerateTriggerTime,r.schedulerNode,r.scheduleFrequency,
      r.lastScheduleTime,r.updateTime)).update((job.className,job.metaData,job.dataTimeOffset,job.dataTimeOffsetUnit
      ,job.startTime,job.cron,job.priority,job.parallel,job.retryTimes,job.workerNodes,job.clusterName
      ,job.groupName,job.timeout,job.replaceIfExist,job.lastGenerateTriggerTime
      ,job.schedulerNode,job.scheduleFrequency,job.lastScheduleTime,job.updateTime))
    db.run(updateAction)
  }

  override def selectOne(jobName: String)(implicit global: ExecutionContext): Future[Option[JobPo]] = {
    val selectAction = tables.filter(_.name === jobName)
    db.run(selectAction.result.headOption).map(_.map(r=>jobRow2bPo(r)))
  }

  /**
    * 选择在一个周期内需要调度的作业
    *
    * @param scheduleNode      调度器节点
    * @param scheduledFireTime 调度周期的时间
    * @param frequencyInSec    调度器周期，单位是秒
    * @param block             对结果以流的形式处理
    */
  override def selectScheduleJob(scheduleNode: String, scheduledFireTime: Long, frequencyInSec: Long)(block: JobPo => Unit)(implicit global: ExecutionContext): Unit = {
    val selectAction = tables.filter(r=>r.schedulerNode === scheduleNode && r.lastScheduleTime-scheduledFireTime<2*frequencyInSec*1000)
    db.stream(selectAction.result).foreach{ row =>
      block(row)
    }
  }

  /**
    * 寻找对应调度器负责调度的job
    *
    * @param scheduleNode 调度器地址
    * @param block        对结果以流的形式处理
    */
  override def selectJobsByScheduleNode(scheduleNode: String)(block: JobPo => Unit)(implicit global: ExecutionContext): Unit = {
    val selectAction = tables.filter(r=>r.schedulerNode === scheduleNode)
    db.stream(selectAction.result).foreach{ row =>
      block(row)
    }
  }

  /**
    * 插入一个数据，触发主键冲突的时候，更新原有数据
    *
    * @param data 待插入的数据
    * @return 插入后的数据（含更新的ID字段等）
    */
  def insertOnDuplicateUpdate(data: JobPo)(implicit global: ExecutionContext): Future[Int] =
    db.run(insertForUpdate(jobPo2Row(data)))

  /**
    * 批量插入JobPo
    *
    * @param jobs 待插入的数据
    * @return 插入的个数
    */
  override def batchInsert(jobs: Array[JobPo])(implicit global: ExecutionContext): Future[Int] = {
    val insert = tables ++= jobs.map(jobPo2Row)
    db.run(insert).map(_.getOrElse(0))
  }
}
