package com.gabry.job.examples

import com.gabry.job.core.task.Task
import com.gabry.job.utils.TaskClassLoader
import org.scalatest.{BeforeAndAfterAll, FunSuite}

/**
  * Created by gabry on 2018/3/28 16:14
  */
class TestTaskExample extends FunSuite with BeforeAndAfterAll{
  val classLoader = new TaskClassLoader("target/lemon-schedule-examples-1.0-SNAPSHOT.jar")

  override def beforeAll(): Unit = {
    super.beforeAll()
    classLoader.init()
  }

  override def afterAll(): Unit = {
    super.afterAll()
    classLoader.destroy()
  }
  test("TestTaskExample task example"){
    val taskClaz = classLoader.loadInstance[Task]("com.gabry.job.examples.TaskExample")
    assert(taskClaz.isSuccess)
    val task = taskClaz.get
    assert(task!=null)
    task.initialize()
    task.destroy()
  }
}
