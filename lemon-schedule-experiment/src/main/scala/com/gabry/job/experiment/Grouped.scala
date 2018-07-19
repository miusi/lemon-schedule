package com.gabry.job.experiment


/**
  * Created by gabry on 2018/5/17 10:05
  */
object Grouped {
  val lst = List(1,2,3)
  var y = 2
  lst.foreach(y_=)
  println(y)
  def main(args: Array[String]): Unit = {
    val array = Array(1,2,3,4,5,6,7,8,9,0,1,2,3,4,5,6)
    array.grouped(3).foreach(grp=>
    println(s"grp=${grp.mkString(",")}"))
    Map("one"->"xxxxxx") match {
      case map1:Map[_,Int] => println("打印1")
      case map2:Map[_,String] => println("打印2")
      case map3:Map[String,String] => println("打印3")
    }
  }
}
