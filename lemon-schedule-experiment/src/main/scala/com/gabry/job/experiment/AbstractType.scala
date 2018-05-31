package com.gabry.job.experiment

import com.gabry.job.experiment.Command.Insert

/**
  * Created by gabry on 2018/5/3 9:17
  */
trait Command[T]{
  def row:T
}
object Command{
  final case class Insert[T](row:T)extends Command[T]
}
object AbstractType {
  def main(args: Array[String]): Unit = {
    val insertInt = Insert(1)
    val insertString = Insert("123")
    insertInt match {
      case Insert(row:Int)=>
        println(s"insertInt is Int $row")
      case other=>
        println(s"insertInt is not Int $other")
    }
    insertString match {
      case Insert(row:String)=>
        println(s"insertString is String $row")
      case other=>
        println(s"insertString is not String $other")
    }

  }
}
