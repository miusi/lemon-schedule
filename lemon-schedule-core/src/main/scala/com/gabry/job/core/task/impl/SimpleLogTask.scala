package com.gabry.job.core.task.impl

import com.gabry.job.core.domain.JobContext
import com.gabry.job.core.task.{Task, TaskLogging}

/**
  * Created by gabry on 2018/4/19 13:22
  * 测试任务，只打印当前参数
  */
class SimpleLogTask extends Task with TaskLogging{
  /**
    * 当前Task是否能取消
    *
    * @return true能取消；false不能取消
    */
  override def canInterrupt: Boolean = true

  /**
    * 执行初始化动作，只执行一次
    */
  override def initialize(): Unit = {
    log.info("task initialized")
  }

  /**
    * 执行具体的业务逻辑
    *
    * @param jobContext 作业执行时的上下文
    * @param taskIndex  作业执行时的索引值，范围 0 ~ （parallel-1）
    * @param parallel   该作业的并发度
    */
  override def run(jobContext: JobContext, taskIndex: Int, parallel: Int): Unit = {
    log.info("task running")
    log.info(s"jobContext=$jobContext,taskIndex=$taskIndex,parallel=$parallel")
    log.info("task succeed")
  }

  /**
    * 资源释放，最多执行一次
    */
  override def destroy(): Unit = {
    log.info("task destroyed")
  }
}
