package com.gabry.job.core.domain

/**
  * Created by gabry on 2018/3/23 17:12
  */
/**
  * 作业执行上下文，作业调度时生成
  * @param job 用户提交的job信息
  * @param schedule 调度计划信息
  * @param dataTime 当前任务的时间
  * @param retryId 当前重试次数
  * @param lastError 作业执行时上一次失败的原因，重试等异常情况下该字段有值
  */
final case class JobContext(job:Job, schedule:Schedule,dataTime:Long,retryId:Int = 0, lastError:Option[Exception] = None ){
  override def toString: String = s"JobContext(job=$job,schedule=$schedule,dataTime=$dataTime,retryId=$retryId,lastError=$lastError"
}

