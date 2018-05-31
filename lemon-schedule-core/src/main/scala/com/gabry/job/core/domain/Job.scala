package com.gabry.job.core.domain

import java.util.concurrent.TimeUnit

import com.gabry.job.core.constant.Constants

import scala.collection.immutable.HashMap
import scala.concurrent.duration.TimeUnit
import scala.util.parsing.json.JSONObject

/**
  * Created by gabry on 2018/3/28 18:58
  */
/**
  * 客户端提交的job类.用来指定作业的执行频率等信息
  * @param name 作业名称，唯一
  * @param className job对应的类名称
  * @param cron job执行的cron表达式
  * @param parallel job的并发度。即同时执行的个数
  * @param meta job的额外参数
  * @param workerNodes job可以执行的节点范围，如果不指定，则随机选择节点
  * @param cluster job对应的集群名称
  * @param group job对应的分组名称
  * @param startTime 作业的开始时间，默认是当前时间
  * @param priority 作业的优先级，数值越大优先级越低，0是最大优先级。默认是最小优先级
  * @param retryTimes 作业执行失败时重试的次数
  * @param timeOut  作业执行时超时时间
  * @param replaceIfExist 如果作业已经存在，是否更新原来作业信息
  * @param uid 作业UID值
  */
case class Job(
                uid:UID,
                name:String,
                className:String,
                cron:String,
                dataTimeOffset:Long = 0,
                dataTimeOffsetUnit:TimeUnit = TimeUnit.MINUTES,
                parallel:Int = Int.MaxValue,
                meta:Map[String,String] = HashMap.empty[String,String],
                workerNodes:Array[String] = Array.empty[String],
                cluster:String = Constants.DEFAULT_CLUSTER_NAME,
                group:String  = Constants.DEFAULT_GROUP_NAME,
                startTime:Long = System.currentTimeMillis(),
                priority:Int = Int.MaxValue,
                retryTimes:Int = 0,
                timeOut:Int = Int.MaxValue,
                replaceIfExist:Boolean = false)extends Entity{
  /**
    * 获取作业参数的JSON格式字符串
    * @return 参数的JSON格式字符串
    */
  def getMetaJsonString():String = JSONObject(meta).toString()

  override def toString: String = s"Job(uid=$uid,name=$name,class=$className,cron=$cron,meta=$meta" +
    s",cluster=$cluster,group=$group,worker=${workerNodes.mkString(",")},start=$startTime,priority=$priority,retry=$retryTimes,timeout=$timeOut" +
    s",replace=$replaceIfExist)"
}