package com.gabry.job.core.builder

import java.util.concurrent.TimeUnit

import com.gabry.job.core.constant.Constants
import com.gabry.job.core.domain.Job
import com.gabry.job.core.tools.UIDGenerator

import scala.collection.mutable

/**
  * Created by gabry on 2018/4/20 13:46
  * Job创建器
  * 主要是解决字段过多时传参不便的问题
  */
object JobBuilder {
  def apply(): JobBuilder = new JobBuilder()
}
class JobBuilder extends Builder[Job]{
  private var name:String = _
  private var className:String = _
  private var cron:String = _
  private var dataTimeOffset:Long = 0
  private var dataTimeOffsetUnit:TimeUnit = TimeUnit.MINUTES
  private var parallel:Int = Int.MaxValue
  private var meta:mutable.Map[String,String] = mutable.HashMap.empty[String,String]
  private var workerNodes:Array[String] = Array.empty[String]
  private var cluster:String = Constants.DEFAULT_CLUSTER_NAME
  private var group:String = Constants.DEFAULT_GROUP_NAME
  private var startTime:Long = System.currentTimeMillis()
  private var priority:Int = Int.MaxValue
  private var retryTimes:Int = 0
  private var timeOut:Int = Int.MaxValue
  private var replaceIfExist:Boolean = false
  def withName(name:String):this.type = {
    this.name = name
    this
  }
  def withClass(className:String):this.type = {
    this.className = className
    this
  }
  def withCron(cron:String):this.type = {
    this.cron = cron
    this
  }
  def withDataTimeOffset(dataTimeOffset:Long):this.type = {
    this.dataTimeOffset = dataTimeOffset
    this
  }
  def withDataTimeOffsetUnit(dataTimeOffsetUnit:TimeUnit):this.type = {
    this.dataTimeOffsetUnit = dataTimeOffsetUnit
    this
  }
  def withParallel(parallel:Int):this.type = {
    this.parallel = parallel
    this
  }
  def withMeta(meta:Map[String,String]):this.type = {
    this.meta ++= meta
    this
  }
  def withMeta(key:String,value:String):this.type = {
    this.meta.put(key,value)
    this
  }
  def withWorkerNodes(workerNodes:Array[String]):this.type = {
    this.workerNodes = workerNodes
    this
  }
  def withCluster(cluster:String):this.type = {
    this.cluster = cluster
    this
  }
  def withGroup(group:String):this.type = {
    this.group = group
    this
  }
  def withStartTime(startTime:Long):this.type = {
    this.startTime = startTime
    this
  }
  def withPriority(priority:Int):this.type = {
    this.priority = priority
    this
  }
  def withRetryTimes(retryTimes:Int):this.type = {
    this.retryTimes = retryTimes
    this
  }
  def withTimeOut(timeOut:Int):this.type = {
    this.timeOut = timeOut
    this
  }
  def withReplaceIfExist(replaceIfExist:Boolean):this.type = {
    this.replaceIfExist = replaceIfExist
    this
  }
  override def build():Job = Job(UIDGenerator.globalUIDGenerator.nextUID(),name,className,cron,dataTimeOffset,dataTimeOffsetUnit,parallel,meta.toMap,workerNodes,cluster,group,startTime,priority,retryTimes,timeOut,replaceIfExist)
}
