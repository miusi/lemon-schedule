package com.gabry.job.core.domain

/**
  * Created by gabry on 2018/4/8 9:05
  * 作业的状态
  * Created:刚创建。即刚被调度器生成
  * Ready:就绪。即被调度器选中，准备执行
  * Executing：执行中。即被TaskTracker接收处于执行中
  * Retry：重试中。即失败后，正在重试，但未执行。
  * Success:执行成功。
  * Error:执行失败。
  */
object TaskStatus extends Enumeration {
  type TaskStatus = Value
  val Created = Value("CREATED")
  val Started = Value("STARTED")
  val Waiting = Value("WAITING")
  val Executing = Value("EXECUTING")
  val Timeout = Value("TIMEOUT")
  val Success = Value("SUCCESS")
  val Failed = Value("FAILED")
  val TimeoutStopped = Value("TIMEOUT")
  val MaxRetryStopped = Value("MAX_RETRY")
  val Overloaded = Value("OVERLOADED")

  def apply(status: String): TaskStatus = TaskStatus.withName(status.toUpperCase)
  implicit def enum2String(status:TaskStatus):String = status.toString
  implicit def str2Enum(status: String):TaskStatus = TaskStatus(status)
}
