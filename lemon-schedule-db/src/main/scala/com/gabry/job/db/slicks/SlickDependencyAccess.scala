package com.gabry.job.db.slicks

import com.gabry.job.core.domain.UID
import com.gabry.job.core.po.DependencyPo
import com.gabry.job.db.access.DependencyAccess
import com.gabry.job.db.slicks.schema.Tables
import slick.jdbc.MySQLProfile.api._

import scala.concurrent.{ExecutionContext, Future}

/**
  * Created by gabry on 2018/4/18 16:57
  */
private[db] class SlickDependencyAccess(db:Database) extends  SlickDataAccess(db) with DependencyAccess {
  private lazy val dependency = Tables.Dependencies
  private lazy val schedules = Tables.Schedules
  private lazy val jobs = Tables.Jobs
  private def insertForUpdate(r: Tables.DependenciesRow):DBIO[Int] =
    sqlu"""INSERT INTO dependences
                          (job_id,
                          depend_job_id,
                          time_offset,
                          time_offset_unit,
                          time_offset_milli_sec,
                          update_time)
                          VALUES
                          (
                          ${r.jobUid},
                          ${r.dependJobUid},
                          ${r.timeOffset},
                          ${r.timeOffsetUnit},
                          ${r.timeOffsetMilliSec},
                          null)
                          on duplicate key update
                               time_offset=VALUES(time_offset),
                               time_offset_unit = VALUES(time_offset_unit),
                               time_offset_milli_sec = VALUES(time_offset_milli_sec),
                               update_time = NULL"""
  /**
    * 插入一个数据
    *
    * @param data 待插入的数据
    * @return 插入后的数据（含更新的ID字段等）
    */
  override def insert(data: DependencyPo)(implicit global: ExecutionContext): Future[DependencyPo] = {
    val insertAction =  (dependency
        returning
        dependency.map(_.rowId)) into ((dependencyPo,newRowId)=>dependencyPo.copy( rowId = newRowId)) += data
    db.run(insertAction).mapTo[DependencyPo]
  }

  /**
    * 插入一个数据，触发主键冲突的时候，更新原有数据
    *
    * @param data 待插入的数据
    * @return 插入后的数据（含更新的ID字段等）
    */
  override def insertOnDuplicateUpdate(data: DependencyPo)(implicit global: ExecutionContext): Future[Int] =
    db.run(insertForUpdate(data))

  /**
    * 删除一个数据
    *
    * @param data 待删除的数据
    * @return 删除的数量
    */
  override def delete(data: DependencyPo)(implicit global: ExecutionContext): Future[Int] = {
    val deleteAction = dependency.filter(r=> r.jobUid === data.jobUid && r.dependJobUid === data.dependJobUid).delete
    db.run(deleteAction)
  }

  /**
    * 更新一个数据
    *
    * @param newData 待更新的数据
    * @return 更新的数量
    */
  override def update(newData: DependencyPo)(implicit global: ExecutionContext): Future[Int] = {
    val updateAction = dependency.filter(r=> r.jobUid === newData.jobUid && r.dependJobUid === newData.dependJobUid).update(newData)
    db.run(updateAction)
  }

  /**
    * 查询一个数据
    *
    * @param dataKey 数据主键
    * @return 查询到的数据
    */
  override def selectOne(dataKey: UID)(implicit global: ExecutionContext): Future[Option[DependencyPo]] = {
    val selectAction = dependency.filter(r=> r.uid === dataKey)
    db.run(selectAction.result.headOption).map(_.map(r=>dependencyRow2Po(r)))
  }

  /**
    * 一次性插入多个多个数据
    *
    * @param data 带插入数据列表
    * @return 插入后的数据列表
    */
  override def insertMany(data: Array[DependencyPo])(implicit global: ExecutionContext): Future[Option[Int]] = {
    val insertAction = dependency ++= data.map(p=>dependencyPo2Row(p))
    db.run(insertAction)
  }

  /**
    * 一次性删除作业所有的依赖
    *
    * @param jobUid 指定的作业ID
    * @return 删除的个数
    */
  override def deleteAllDependency(jobUid: UID)(implicit global: ExecutionContext): Future[Int] = {
    val deleteAction = dependency.filter(r=>r.jobUid === jobUid).delete
    db.run(deleteAction)
  }

  /**
    * 查询任务的依赖
    * @param jobUid 当前作业ID
    * @param dataTime 当前作业数据时间
    * @return true依赖满足，false依赖不满足
    */
  override def selectSucceedState(jobUid:UID, dataTime:Long )(implicit global: ExecutionContext):Future[Boolean] = {
    // 全部成功，而且succeed字段是true
    val selectAction = jobs.filter(_.uid === jobUid).joinLeft(dependency).on{ (j,d)=>j.uid === d.jobUid }.map{
      case (j,d)=> (j.uid,d.map(r=>r.dependJobUid),d.map(r=>r.timeOffsetMilliSec).getOrElse(0L))
    }.joinLeft(schedules).on{ (jd,s) =>
      jd._2 === s.jobUid && jd._3 + dataTime === s.dataTime
    }.map{case (jd,s)=>
      (jd._1,jd._2,s.map(_.succeed))
    }
    // 如果没有依赖，上面也会返回false
    // 有结果，并且结果中有false和没有结果的情况是不一样的
    val selectResult = db.run(selectAction.result).map(res=>
      res.forall{
        case (_,Some(dependJobId),Some(dependJobState)) =>
          dependJobState
        case (_,Some(dependJobId),None)=>
          false
        case (_,None,_)=>
          true
      }
    )
    selectResult
  }

  /**
    * 在一个事务里一次性删除所有依赖，并插入多个依赖
    *
    * @param jobUid 待删除的作业ID
    * @param data  待插入的事务
    * @return 插入的最终结果
    */
  override def deleteAllAndInsertMany(jobUid: UID, data: Array[DependencyPo])(implicit global: ExecutionContext): Future[Option[Int]] = {
    val deleteAction = dependency.filter(r=>r.jobUid === jobUid).delete
    val insertAction = dependency ++= data.map(p=>dependencyPo2Row(p))
    val deleteAndInsert = for{
      _<-deleteAction
      i<-insertAction
    }yield i
    db.run(deleteAndInsert.transactionally)
  }
}
