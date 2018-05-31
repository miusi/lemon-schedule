package com.gabry.job.db.proxy.command

import akka.actor.ActorRef
import com.gabry.job.core.command.ReplyCommand
import com.gabry.job.core.domain.UID
import com.gabry.job.db.DataTables.DataTables

/**
  * Created by gabry on 2018/5/14 10:36
  */
/**
  * 数据库命令包含以下3个重要数据
  * 待操作的数据：row/rowKey
  * 操作成功后的消息接收者:replyTo
  * 引起数据库操作的源命令行消息：replyEvent
  * actor收到对应的消息后Insert/Delete/Update/Select/InsertOnDuplicateUpdate等，进行对应的操作。
  * 操作成功后，将对应的消息结果以及replyEvent发送给replyTo
  */
trait DatabaseCommand[+T] extends ReplyCommand{
  def originEvent:AnyRef
  def row:T
}
object DatabaseCommand{
  final case class Insert[T](row:T, replyTo:ActorRef, originEvent:AnyRef) extends DatabaseCommand[T]{
    override def toString: String = s"Insert(row=$row,replyTo=$replyTo,originEvent=$originEvent)"
  }
  final case class Delete[T](row:T, replyTo:ActorRef, originEvent:AnyRef) extends DatabaseCommand[T]{
    override def toString: String = s"Delete(row=$row,replyTo=$replyTo,originEvent=$originEvent)"
  }
  final case class Update[T](oldRow:T, row:T, replyTo:ActorRef, originEvent:AnyRef) extends DatabaseCommand[T]{
    override def toString: String = s"Update(oldRow=$oldRow,row=$row,replyTo=$replyTo,originEvent=$originEvent)"
  }
  final case class UpdateField(table:DataTables,row:UID,fields:Array[String], replyTo:ActorRef, originEvent:AnyRef) extends DatabaseCommand[UID]{
    override def toString: String = s"UpdateField(table=$table,rowId=$row,fields=[${fields.mkString(",")}],replyTo=$replyTo,originEvent=$originEvent)"
  }
  case class Select[T](row:T, replyTo:ActorRef, originEvent:AnyRef) extends DatabaseCommand[T]{
    override def toString: String = s"Select(row=$row,replyTo=$replyTo,originEvent=$originEvent)"
  }
  final case class InsertOnDuplicateUpdate[T](row:T, replyTo:ActorRef, originEvent:AnyRef) extends DatabaseCommand[T]{
    override def toString: String = s"InsertOnDuplicateUpdate(row=$row,replyTo=$replyTo,originEvent=$originEvent)"
  }
}