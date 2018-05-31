package com.gabry.job.db.proxy.event

import com.gabry.job.core.domain.UID
import com.gabry.job.core.event.Event
import com.gabry.job.db.DataTables.DataTables

/**
  * Created by gabry on 2018/4/25 14:41
  */
/**
  * 数据库操作相关的事件
  * @tparam T 操作的数据库实体类型
  */
trait DatabaseEvent[T] extends Event{
  /**
    * 操作的数据行
    * @return 操作的数据行
    */
  def row:Option[T]

  /**
    * 原始的命令。由于是异步消息，所以需要保存操作数据库之前的命令明细
    * @return 原始的命令
    */
  def originCommand:AnyRef
}
object DatabaseEvent {

  /**
    * 查询事件
    * @param row 查询到的数据
    * @tparam T 操作的数据库实体类型
    */
  final case class Selected[T](row:Option[T],originCommand:AnyRef) extends DatabaseEvent[T]

  /**
    * 插入事件
    * @param row 插入后的数据
    * @tparam T 操作的数据库实体类型
    */
  final case class Inserted[T](row:Option[T],originCommand:AnyRef) extends DatabaseEvent[T]

  /**
    * 删除事件
    * @param row 删除的数据
    * @tparam T 操作的数据库实体类型
    */
  final case class Deleted[T](row:Option[T],originCommand:AnyRef)extends DatabaseEvent[T]

  /**
    * 批量插入事件
    * @param insertedNum 插入的个数
    * @param row 插入数据的一条数据。
    * @tparam T 操作的数据库实体类型
    */
  final case class BatchInserted[T](insertedNum:Int, row:Option[T], originCommand:AnyRef) extends DatabaseEvent[T]

  /**
    * 更新事件
    * @param oldRow 更新前的数据
    * @param row 更新后的数据
    * @tparam T 操作的数据库实体类型
    */
  final case class Updated[T](oldRow:T,row:Option[T],originCommand:AnyRef) extends DatabaseEvent[T]

  /**
    * 特定一个字段的更新事件
    * -@param field 更新的字段
    * -@param row 待更新的数据
    * -@tparam T 操作的数据库实体类型
    */
  final case class FieldUpdated2[T](field:String,row:Option[T],originCommand:AnyRef) extends DatabaseEvent[T]
  /**
    * 特定一个字段的更新事件
    * @param table 待更新的表
    * @param field 新的字段
    * @param row 待更新的数据
    */
  final case class FieldUpdated(table:DataTables,field:Array[String],updatedNum:Int,row:Option[UID],originCommand:AnyRef) extends DatabaseEvent[UID]

}
