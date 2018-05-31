package com.gabry.job.db.access

import com.gabry.job.core.domain.UID
import com.gabry.job.core.po.DependencyPo

import scala.concurrent.{ExecutionContext, Future}

/**
  * Created by gabry on 2018/4/18 16:56
  */
trait DependencyAccess extends DataAccess[UID,DependencyPo]{
  /**
    * 一次性插入多个多个数据
    * @param data 带插入数据列表
    * @return 插入后的数据列表
    */
  def insertMany(data: Array[DependencyPo])(implicit global: ExecutionContext): Future[Option[Int]]

  /**
    * 一次性删除作业所有的依赖
    * @param jobUid 指定的作业ID
    * @return 删除的个数
    */
  def deleteAllDependency(jobUid:UID)(implicit global: ExecutionContext): Future[Int]

  /**
    * 在一个事务里一次性删除所有依赖，并插入多个依赖
    * @param jobUid 待删除的作业ID
    * @param data 待插入的事务
    * @return 插入的最终结果
    */
  def deleteAllAndInsertMany(jobUid:UID, data: Array[DependencyPo])(implicit global: ExecutionContext): Future[Option[Int]]
  /**
    * 查询任务的依赖
    * @param jobUid 当前作业ID
    * @param dataTime 当前作业数据时间
    * @return true依赖满足，false依赖不满足
    */
  def selectSucceedState(jobUid:UID, dataTime:Long )(implicit global: ExecutionContext):Future[Boolean]
}
