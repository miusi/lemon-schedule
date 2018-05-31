package com.gabry.job

import java.util.concurrent.TimeUnit

import com.gabry.job.client.akkaclient.AkkaJobClient
import com.gabry.job.core.builder.{DependencyBuilder, JobBuilder}
import com.gabry.job.utils.Utils
import com.typesafe.config.ConfigFactory

/**
  * Created by gabry on 2018/4/20 17:38
  */
object TestDependency {
  def main(args: Array[String]): Unit = {
    val config = ConfigFactory.load()
    val client = new AkkaJobClient(config)
    client.start()

    val firstJobName = "249075951481080726"
    val secondJobName = "249075951481080726".reverse

    val meta = Map("currentDate"->Utils.formatDate(System.currentTimeMillis()))
    val firstJob =JobBuilder()
      .withName(firstJobName)
      .withClass("com.gabry.job.examples.TaskExample")
      .withCron("0/1 * * * *")
      .withGroup("task_tracker_example")
      .withRetryTimes(3)
      .withMeta(meta)
      .build()
    val secondJob =JobBuilder()
      .withName(secondJobName)
      .withClass("com.gabry.job.examples.TaskExample")
      .withCron("0/1 * * * *")
      .withGroup("task_tracker_example")
      .withRetryTimes(3)
      .withMeta(meta)
      .build()

    println(s"firstJobName=$firstJobName")
    client.submitJob(firstJob)
    Thread.sleep(2*1000)

    val dependency = DependencyBuilder()
      .withDependJobId("1")
      .withTimeOffset(0)
      .withTimeOffsetUnit(TimeUnit.MINUTES)
      .build()

    client.submitJob(secondJob,Array(dependency))
    Thread.sleep(5*1000)
    client.stop()
  }
}
