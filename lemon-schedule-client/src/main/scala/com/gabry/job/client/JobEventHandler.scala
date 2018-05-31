package com.gabry.job.client

/**
  * Created by gabry on 2018/4/4 9:26
  * 作业执行事件的handler
  */
trait JobEventHandler {
  def handle():Unit
}
