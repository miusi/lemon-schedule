package com.gabry.job
import com.gabry.job.client.akkaclient.AkkaJobClient
import com.gabry.job.core.builder.JobBuilder
import com.gabry.job.utils.Utils
import com.typesafe.config.ConfigFactory

/**
 * Hello world!
 *
 */
object TestClient{
  def main(args: Array[String]): Unit = {
    val config = ConfigFactory.load()
    val client = new AkkaJobClient(config)
    client.start()

    val jobName = "249075951481080726"
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

    println(s"jobName=$jobName")
    client.submitJob(job)
    Thread.sleep(5*1000)
    client.stop()
  }
}
