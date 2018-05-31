package com.gabry.job.core.task.impl

import java.io.File

import com.gabry.job.core.builder.CommandJobBuilder
import com.gabry.job.core.domain.JobContext
import com.gabry.job.core.task.{Task, TaskLogging}

import scala.sys.process.{Process, ProcessLogger}

/**
  * Created by gabry on 2018/4/16 13:12
  * 命令行任务。
  * 接收参数，执行对应的命令。例如 echo Hello World
  */
class CommandTask extends Task with TaskLogging{
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
    log.info("initialize")
  }

  /**
    * 执行具体的业务逻辑
    *
    * @param jobContext 作业执行时的上下文
    * @param taskIndex  作业执行时的索引值，范围 0 ~ （parallel-1）
    * @param parallel   该作业的并发度
    */
  override def run(jobContext: JobContext, taskIndex: Int, parallel: Int): Unit = {
    log.info(s"jobContext = $jobContext,taskIndex = $taskIndex,parallel = $parallel")
    val command = jobContext.job.meta.getOrElse(CommandJobBuilder.COMMAND_KEY, "")
    val workDir = jobContext.job.meta.get(CommandJobBuilder.WORK_DIR_KEY).map(path=>new File(path))
    val env = jobContext.job.meta.toArray
    val proc = Process(command,workDir,env: _*)
    var exitCode = 127
    try{
      exitCode = proc ! ProcessLogger(out => log.info(s"stdout:$out"),err => log.error(s"stderr:$err"))
    }catch {
      case exception:Throwable =>
        log.error(exception.getMessage,exception)
    }
    log.info(s"[$command] exit code is $exitCode")
  }

  /**
    * 资源释放，最多执行一次
    */
  override def destroy(): Unit = {
    log.info("destroy")
  }
}
