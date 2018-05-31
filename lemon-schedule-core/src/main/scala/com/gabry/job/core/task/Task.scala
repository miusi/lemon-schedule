package com.gabry.job.core.task

import com.gabry.job.core.domain.JobContext

/**
  * Created by gabry on 2018/3/23 16:56
  * 一个Job执行的实例。
  * 执行具体的业务逻辑
  */
trait Task {
  /**
    * 当前Task是否能取消
    * @return true能取消；false不能取消
    */
  def canInterrupt:Boolean

  /**
    * 执行初始化动作，只执行一次
    */
  def initialize():Unit

  /**
    * 执行具体的业务逻辑
    * @param jobContext 作业执行时的上下文
    * @param taskIndex 作业执行时的索引值，范围 0 ~ （parallel-1）
    * @param parallel 该作业的并发度
    */
  def run(jobContext: JobContext, taskIndex:Int, parallel:Int):Unit

  /**
    * 资源释放，最多执行一次
    */
  def destroy():Unit

  override def toString: String = this.getClass.getName
}
