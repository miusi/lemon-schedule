package com.gabry.job.scheduler.daemon

import com.gabry.job.core.daemon.DaemonCreator
import com.gabry.job.scheduler.node.JobSchedulerNode

import scala.util.{Failure, Success}


/**
  * Created by gabry on 2018/4/2 10:26
  * 启动jobScheduler节点
  */
object JobSchedulerDaemon {
  def main(args: Array[String]): Unit = {
    val port = args.headOption.map(_.toInt)

    DaemonCreator.createDaemon(JobSchedulerNode,port) match {
      case Success(daemon) =>
        println(s"JobSchedulerNode start at [${daemon.actor}]")
      case Failure(reason) =>
        println(s"JobSchedulerNode start failed ,reason: $reason")
        reason.printStackTrace()
    }
  }
}
