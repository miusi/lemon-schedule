package com.gabry.job.db.access

import scala.concurrent.{ExecutionContext, Future}

/**
  * Created by gabry on 2018/3/30 15:03
  * 数据存取的异步接口
  * 不再提供同步操作数据的接口，全都异步化。
  */
trait DataAccess[K,T]{
  /**
    * 插入一个数据
    * @param data 待插入的数据
    * @return 插入后的数据（含更新的ID字段等）
    */
  def insert(data:T)(implicit global: ExecutionContext):Future[T]

  /**
    * 插入一个数据，触发主键冲突的时候，更新原有数据
    * @param data 待插入的数据
    * @return 插入后的数据（含更新的ID字段等）
    */
  def insertOnDuplicateUpdate(data:T)(implicit global: ExecutionContext):Future[Int]
  /**
    * 删除一个数据
    * @param data 待删除的数据
    * @return 删除的数量
    */
  def delete(data:T)(implicit global: ExecutionContext):Future[Int]

  /**
    * 更新一个数据
    * @param newData 待更新的数据
    * @return 更新的数量
    */
  def update(newData: T)(implicit global: ExecutionContext):Future[Int]

  /**
    * 查询一个数据
    * @param dataKey 数据主键
    * @return 查询到的数据
    */
  def selectOne(dataKey:K)(implicit global: ExecutionContext): Future[Option[T]]
}
