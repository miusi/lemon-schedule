package com.gabry.job.client

import com.gabry.job.core.domain.{Dependency, Job}

/**
  * Created by gabry on 2018/4/24 15:26
  * JobClient接口
  */
// TODO: 2018年4月24日15:31:56 紧急。需要修改接口的形式，不能简单的把日志信息打印出来，这样不利于调用方排错
trait JobClient {

  /**
    * 当前客户端是否启动
    * @return true启动
    */
  def isStarted:Boolean
  /**
    * 启动客户端
    */
  def start():Unit

  /**
    * 提交作业
    * @param job 待提交的作业
    * @param dependency 作业的依赖
    */
  def submitJob(job:Job,dependency: Array[Dependency]):Unit
  /**
    * 提交作业
    * @param job 待提交的作业
    */
  def submitJob(job:Job):Unit = submitJob(job,Array.empty[Dependency])

  /**
    * 取消作业执行。执行中的task会正常运行结束
    * @param jobId 待取消的作业ID
    */
  final def cancelJob(jobId:Long):Unit = cancelJob(jobId,force = false)

  /**
    * 取消作业执行
    * @param jobId 待取消的作业ID
    * @param force true强制停止作业，会杀掉执行中的task
    *              false只会删除相关记录，会等执行中的task正常运行结束
    */
  def cancelJob(jobId:Long,force:Boolean):Unit
  /**
    * 停止客户端
    */
  def stop():Unit
}
