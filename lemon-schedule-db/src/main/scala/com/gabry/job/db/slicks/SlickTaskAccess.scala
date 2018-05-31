package com.gabry.job.db.slicks

import com.gabry.job.core.domain.UID
import com.gabry.job.core.po.TaskPo
import com.gabry.job.db.access.TaskAccess
import com.gabry.job.db.slicks.schema.Tables
import slick.jdbc.MySQLProfile.api._

import scala.concurrent.{ExecutionContext, Future}
/**
  * Created by gabry on 2018/4/10 17:04
  */
private[db] class SlickTaskAccess(db:Database) extends SlickDataAccess(db) with TaskAccess{
  private lazy val tables = Tables.Tasks
  private def insertForUpdate(r: Tables.TasksRow):DBIO[Int] =
    sqlu"""INSERT INTO tasks
                        (uid,job_uid,
                        schedule_uid,
                        retry_id,
                        task_tracker_node,
                        state,
                        event_time,
                        message,
                        update_time)
                        VALUES
                        (${r.uid},
                        ${r.jobUid},
                        ${r.scheduleUidd},
                        ${r.retryId},
                        ${r.taskTrackerNode},
                        ${r.state},
                        ${r.eventTime},
                        ${r.message},null)
                        on duplicate key update
                          task_tracker_node=values(task_tracker_node),
                          event_time=values(event_time),
                          message=values(message),
                          update_time=null"""
  /**
    * 插入一个数据
    *
    * @param data 待插入的数据
    * @return 插入后的数据（含更新的ID字段等）
    */
  override def insert(data: TaskPo)(implicit global: ExecutionContext): Future[TaskPo] = {
    val insertAction = (tables
      returning
      tables.map(_.rowId)) into ((task,newId)=>task.copy(rowId=newId)) += data
    db.run(insertAction).map(taskRow2Po)
  }

  /**
    * 删除一个数据
    *
    * @param data 待删除的数据
    * @return 删除的数量
    */
  override def delete(data: TaskPo)(implicit global: ExecutionContext): Future[Int] = {
    val deleteAction = tables.filter(_.uid === data.uid).delete
    db.run(deleteAction)
  }

  /**
    * 更新一个数据
    *
    * @param newData 待更新的数据
    * @return 更新的数量
    */
  override def update(newData: TaskPo)(implicit global: ExecutionContext): Future[Int] = {
    val updateAction = tables.filter(_.uid === newData.uid).update(newData)
    db.run(updateAction)
  }

  /**
    * 查询一个数据
    *
    * @param dataKey 数据主键
    * @return 查询到的数据
    */
  override def selectOne(dataKey: UID)(implicit global: ExecutionContext): Future[Option[TaskPo]] = {
    val selectAction = tables.filter(_.uid === dataKey)
    db.run(selectAction.result.headOption).map(_.map(r=>taskRow2Po(r)))
  }

  /**
    * 插入一个数据，触发主键冲突的时候，更新原有数据
    *
    * @param data 待插入的数据
    * @return 插入后的数据（含更新的ID字段等）
    */
  override def insertOnDuplicateUpdate(data: TaskPo)(implicit global: ExecutionContext): Future[Int] =
    db.run(insertForUpdate(data))

  override def batchInsert(batch: Array[TaskPo])(implicit global: ExecutionContext): Future[Int] = {
    val insert = tables.++=(batch.map(taskPo2Row))
    db.run(insert).map(_.getOrElse(0))
  }
}
