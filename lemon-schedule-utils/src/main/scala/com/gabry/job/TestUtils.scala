package com.gabry.job

import com.gabry.job.utils.Utils

/**
 * Hello world!
 */
object TestUtils{
  def main(args: Array[String]): Unit = {
    Utils.getLoadedClass(ClassLoader.getSystemClassLoader).foreach(println)
    Utils.getLoadedClass(this.getClass.getClassLoader).foreach(println)
    println("生成结束")
  }
}
