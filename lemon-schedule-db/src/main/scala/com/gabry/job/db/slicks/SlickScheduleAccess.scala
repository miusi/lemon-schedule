package com.gabry.job.db.slicks

import com.gabry.job.core.domain.UID
import com.gabry.job.core.po.SchedulePo
import com.gabry.job.db.access.ScheduleAccess
import com.gabry.job.db.slicks.schema.Tables
import slick.jdbc.MySQLProfile.api._

import scala.concurrent.{ExecutionContext, Future}
/**
  * Created by gabry on 2018/4/2 10:18
  */
private[db] class SlickScheduleAccess(db:Database) extends SlickDataAccess(db) with ScheduleAccess{
  private lazy val tables = Tables.Schedules

  private def insertForUpdate(r: SchedulePo):DBIO[Int] =
    sqlu"""INSERT INTO schedules
                      (job_id,
                      priority,
                      retry_times,
                      dispatched,
                      trigger_time,
                      schedule_node,
                      schedule_time,
                      succeed,
                      data_time,
                      update_time)
                      VALUES
                      (${r.jobUid},
                        ${r.priority},
                        ${r.retryTimes},
                        ${r.dispatched},
                        ${r.triggerTime},
                        ${r.scheduleNode},
                        ${r.scheduleTime},
                        ${r.succeed},
                        ${r.dataTime},
                        null)
                        on duplicate key update
                          priority=values(priority),
                          retry_times=values(retry_times),
                          dispatched=values(dispatched),
                          schedule_node=values(schedule_node),
                          schedule_time=values(schedule_time),
                          succeed=values(succeed),
                          data_time=values(data_time),
                          update_time=null"""
  /**
    * 插入一个schedule
    *
    * @param schedule 插入后的schedule
    * @return 插入的数量
    */
  override def insert(schedule: SchedulePo)(implicit global: ExecutionContext): Future[SchedulePo] = {
    val insertAction = (tables
      returning
      tables.map(_.rowId)) into ((data,newId)=>data.copy(rowId=newId)) += schedule
    db.run(insertAction).map(r=>scheduleRow2Po(r))
  }

  /**
    * 删除一个schedule
    *
    * @param task 待删除的schedule
    * @return 删除的数量
    */
  override def delete(task: SchedulePo)(implicit global: ExecutionContext): Future[Int] = {
    val deleteAction = tables.filter(_.uid === task.uid).delete
    db.run(deleteAction)
  }

  /**
    * 更新一个schedule
    *
    * @param newSchedule 待更新的schedule
    * @return 更新的数量
    */
  override def update(newSchedule: SchedulePo)(implicit global: ExecutionContext): Future[Int] = {
    val updateAction = tables.filter(r=>r.jobUid === newSchedule.jobUid && r.triggerTime === newSchedule.triggerTime).update(newSchedule)
    db.run(updateAction)
  }

  /**
    * 查询一个schedule
    *
    * @param jobIdAndTriggerTime 作业ID和触发时间
    * @return 查询到的schedule
    */
  override def selectOne(jobIdAndTriggerTime: (UID,Long))(implicit global: ExecutionContext): Future[Option[SchedulePo]] = {
    val selectAction = tables.filter(r=>r.jobUid ===jobIdAndTriggerTime._1 && r.triggerTime === jobIdAndTriggerTime._2)
    db.run(selectAction.result.headOption).map(_.map(r=>scheduleRow2Po(r)))
  }
  /**
    * 根据scheduleNode/triggerTime查询多个数据，并以流的形式调用block代码处理每个结果
    * @param scheduleNode 调度器节点
    * @param triggerTime 触发时间
    * @param block 数据处理block
    */
  override def selectUnDispatchSchedule(jobUid:UID, scheduleNode: String, triggerTime: Long, maxNum:Int)(block: SchedulePo => Unit)(implicit global: ExecutionContext): Unit = {
    val selectAction = tables
      .filter(r=>r.jobUid === jobUid && r.scheduleNode === scheduleNode && r.triggerTime <= triggerTime && r.dispatched === false )
      .take(maxNum)

    db.stream(selectAction.result).foreach{ row =>
      block(row)
    }
  }

  /**
    * 将当前计划设置为已调度
    * @param scheduleUidd 作业ID
    */
  override def setDispatched(scheduleUidd: UID, dispatched:Boolean)(implicit global: ExecutionContext):Future[Int] = {
    val updateAction = tables.filter(_.uid === scheduleUidd)
      .map(r=>(r.dispatched, r.updateTime)).update((dispatched,null))
    db.run(updateAction)
  }

  /**
    * 插入一个数据，触发主键冲突的时候，更新原有数据
    * @param data 待插入的数据
    *
    * @return 插入后的数据（含更新的ID字段等）
    */
  override def insertOnDuplicateUpdate(data: SchedulePo)(implicit global: ExecutionContext): Future[Int] =
    db.run(insertForUpdate(data))
  /**
    * 将当前计划设置为成功
    * @param scheduleUidd 计划ID
    * @param succeed 计划成功标志
    * @return 更新的行数
    */
  def setSucceed(scheduleUidd:UID, succeed:Boolean)(implicit global: ExecutionContext):Future[Int] = {
    val updateAction = tables.filter(_.uid === scheduleUidd)
      .map(r=>(r.succeed, r.updateTime)).update((succeed,null))
    db.run(updateAction)
  }

}
