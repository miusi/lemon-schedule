package com.gabry.job.experiment

/**
  * Created by gabry on 2018/5/17 10:05
  */
object Grouped {
  def main(args: Array[String]): Unit = {
    val array = Array(1,2,3,4,5,6,7,8,9,0,1,2,3,4,5,6)
    array.grouped(3).foreach(grp=>
    println(s"grp=${grp.mkString(",")}"))
  }
}
