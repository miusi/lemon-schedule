package com.gabry.job.core

/**
  * Created by gabry on 2018/5/21 10:30
  */
trait Message {
  /**
    * 消息发生的时间
    */
  final val at: Long = System.currentTimeMillis()
}
