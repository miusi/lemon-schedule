package com.gabry.job.experiment

import java.util.concurrent.{Callable, FutureTask}

import com.gabry.job.utils.TaskClassLoader

/**
 * Hello world!
 *
 */
object CancelFutureTask {
  def main(args: Array[String]): Unit = {
    //D:/MyCode/lemon-schedule/lemon-schedule-examples/target/lemon-schedule-examples-1.0-SNAPSHOT.jar
    val classLoader = new TaskClassLoader("D:/MyCode/lemon-schedule/lemon-schedule-examples/target/lemon-schedule-examples-1.0-SNAPSHOT.jar")
    classLoader.init()
    classLoader.destroy()
    import scala.concurrent._
    import ExecutionContext.Implicits.global
    class Task extends Callable[Int]{
      override def call(): Int = {
        println(s"thread [${Thread.currentThread().getName}]")
        val count = 5
        0 until count foreach { i=>
          println(s"task alive $i")
          Thread.sleep(1000)
        }
        count
      }
    }
    val futureTask = new FutureTask[Int](new Task)
    Future{
      futureTask.run()
      println(s"futureTask执行成功 ${futureTask.get()}")
    }
    Thread.sleep(3*1000)
    println("futureTask提前终止")
    futureTask.cancel(true)
    Thread.sleep(3*1000)
    println(s"futureTask执行 ${futureTask.get()}")
  }
}
