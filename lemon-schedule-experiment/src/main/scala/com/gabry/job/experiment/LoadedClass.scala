package com.gabry.job.experiment

/**
  * Created by gabry on 2018/4/28 10:11
  */
object LoadedClass {
  def main(args: Array[String]): Unit = {
    val classLoader = ClassLoader.getSystemClassLoader
    val field = classOf[ClassLoader].getDeclaredField("classes")
    field.setAccessible(true)
    val loadedClass = field.get(classLoader).asInstanceOf[java.util.Vector[_]].elements()
    while(loadedClass.hasMoreElements){
      println(loadedClass.nextElement())
    }
  }
}
