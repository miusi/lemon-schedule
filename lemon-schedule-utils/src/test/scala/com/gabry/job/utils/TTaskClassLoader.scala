package com.gabry.job.utils

import org.scalatest.{BeforeAndAfterAll, FunSuite}

/**
  * Created by gabry on 2018/4/12 11:07
  * 测试类加载器
  */
class TTaskClassLoader extends FunSuite with BeforeAndAfterAll{
  val taskClassLoader = new TaskClassLoader("../lemon-schedule-examples/target/lemon-schedule-examples-1.0-SNAPSHOT.jar")
  override def beforeAll(): Unit = {
    super.beforeAll()
    taskClassLoader.init()
  }

  override def afterAll(): Unit = {
    super.afterAll()
    taskClassLoader.destroy()

  }
  test("taskClassLoader"){
//    intercept[NoClassDefFoundError]{
      taskClassLoader.load("com.gabry.job.examples.TestTask")
      // 此处不太方便测试，不再单独编写测试
  //  }
  }
}
