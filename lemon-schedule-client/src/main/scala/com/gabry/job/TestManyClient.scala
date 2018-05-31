package com.gabry.job

import java.util.UUID

import com.gabry.job.client.akkaclient.AkkaJobClient
import com.gabry.job.core.builder.JobBuilder
import com.gabry.job.utils.Utils
import com.typesafe.config.ConfigFactory

/**
  * Created by gabry on 2018/4/8 18:03
  */
object TestManyClient {
  def main(args: Array[String]): Unit = {
    val config = ConfigFactory.load()
    val client = new AkkaJobClient(config)
    client.start()
    val maxJob = args(0).toInt

    0 until maxJob foreach { i =>
      val jobName = UUID.randomUUID().getMostSignificantBits.toString
      val meta = Map("currentDate"->Utils.formatDate(System.currentTimeMillis()))
      val job =JobBuilder()
        .withName(jobName)
        .withClass("com.gabry.job.examples.TaskExample")
        .withCron("0/1 * * * *")
        .withGroup("task_tracker_example")
        .withRetryTimes(3)
        .withMeta(meta)
        .withReplaceIfExist(true)
        .build()

      //val job = Job(jobName,"com.gabry.job.examples.TestTask","0/1 * * * *",-1,TimeUnit.MINUTES,group = "task_tracker_example",retryTimes = 3,replaceIfExist = true)
      println(s"jobName=$jobName")
      client.submitJob(job)
      //Thread.sleep(10)
    }
    Thread.sleep(10*1000)
    print(s"$maxJob 个作业已经提交")
    client.stop()
  }

}
