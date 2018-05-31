package com.gabry.job.examples

import com.gabry.job.core.domain.JobContext
import com.gabry.job.core.task.{Task, TaskLogging}

/**
  * Created by gabry on 2018/3/27 17:39
  * 测试Task接口
  */
class TaskExample extends Task with TaskLogging{
  /**
    * 当前Task是否能取消
    *
    * @return true能取消；false不能取消
    */
  override def canInterrupt: Boolean = false

  /**
    * 执行初始化动作，只执行一次
    */
  override def initialize(): Unit = {
   log.info("TestTask 开始初始化")
  }

  /**
    * 执行具体的业务逻辑
    *
    * @param jobContext 作业执行时的上下文
    * @param taskIndex  作业执行时的索引值，范围 0 ~ （parallel-1）
    * @param parallel   该作业的并发度
    */
  override def run(jobContext: JobContext, taskIndex: Int, parallel: Int): Unit = {
//    println("作业执行中休眠3秒")
//    println(s"jobContext $jobContext")
//    Thread.sleep(3*1000)
//    println("作业执行完毕")
    log.info(s"jobContext=$jobContext,taskIndex=$taskIndex,parallel=$parallel")
//    taskIndex match {
//      case 0 =>
//        log.info("第一次执行，抛出异常")
//        throw new Exception("作业第一次执行，抛出异常，进行测试")
//      case 1=>
//        val sleepSeconds = 5
//        log.info(s"执行正常的业务逻辑，休眠 $sleepSeconds 秒")
//        0 until sleepSeconds foreach { i =>
//          log.info(s"当前作业中i = $i")
//          Thread.sleep(1000)
//        }
//        log.info("业务逻辑执行完毕")
//      case _=>
//    }
  }

  /**
    * 资源释放，最多执行一次
    */
  override def destroy(): Unit = {
    log.info("TestTask 开始释放资源")
  }

}
