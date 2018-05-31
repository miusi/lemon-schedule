package com.gabry.job.worker.daemon

import com.gabry.job.core.daemon.DaemonCreator
import com.gabry.job.worker.node.TaskWorkerNode

import scala.util.{Failure, Success}

/**
  * Created by gabry on 2018/3/26 14:21
  * 启动TaskWorkerNode节点
  */
object TaskWorkerDaemon{

  def main(args: Array[String]): Unit = {
    val port = args.headOption.map(_.toInt)

    DaemonCreator.createDaemon(TaskWorkerNode,port) match {
      case Success(daemon) =>
        println(s"TaskWorkerNode start at [${daemon.actor}]")
      case Failure(reason) =>
        println(s"TaskWorkerNode start failed ,reason: $reason")
        reason.printStackTrace()
    }
  }
}
