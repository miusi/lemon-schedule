package com.gabry.job.utils

import java.time.temporal.ChronoField
import java.time.{Instant, ZoneId, ZonedDateTime}
import java.util.Locale

import com.cronutils.descriptor.CronDescriptor
import com.cronutils.model.CronType
import com.cronutils.model.definition.CronDefinitionBuilder
import com.cronutils.model.time.ExecutionTime
import com.cronutils.parser.CronParser

/**
  * Created by gabry on 2018/3/30 16:15
  * cron表达式计算类
  */
object CronGenerator {
  private val cronDefinition = CronDefinitionBuilder.instanceDefinitionFor(CronType.UNIX)
  private val parser = new CronParser(cronDefinition)
  private lazy val descriptor = CronDescriptor.instance(Locale.ENGLISH)
  private val zoneIdKey = "Asia/Shanghai"
  private val zoneId = ZoneId.of(zoneIdKey)
  /**
    * 根据当前时间获取上一次执行时间
    * @param cronExpression cron表达式
    * @param timeBefore 指定时间
    * @return 上一次执行时间
    */
  private def getPreviousTriggerTime(cronExpression:String,timeBefore:ZonedDateTime):Option[Long] = {
    try {
      val cron = parser.parse(cronExpression)
      val executionTime = ExecutionTime.forCron(cron)
      val javaOptionalDate = executionTime.lastExecution(timeBefore)
      if(javaOptionalDate.isPresent){
        val nextExecution = javaOptionalDate.get()
        Some(nextExecution.getLong(ChronoField.INSTANT_SECONDS)*1000+nextExecution.getLong(ChronoField.MILLI_OF_SECOND))
      }
      else None
    }catch {
      case ex:Exception =>
        None
    }
  }
  /**
    * 根据当前时间获取下一次执行时间
    * @param cronExpression cron表达式
    * @param timeAfter 起始时间
    * @return 下一次执行时间
    */
  private def getNextTriggerTime(cronExpression:String,timeAfter:ZonedDateTime):Option[Long] = {
    try {
      val cron = parser.parse(cronExpression)
      val executionTime = ExecutionTime.forCron(cron)
      val javaOptionalDate = executionTime.nextExecution(timeAfter)
      if(javaOptionalDate.isPresent){
        val nextExecution = javaOptionalDate.get()
        Some(nextExecution.getLong(ChronoField.INSTANT_SECONDS)*1000+nextExecution.getLong(ChronoField.MILLI_OF_SECOND))
      }
      else None
    }catch {
      case ex:Exception =>
        None
    }
  }
  /**
    * 根据当前时间获取下一次执行时间
    * @param cronExpression cron表达式
    * @return 下一次执行时间
    */
  def getNextTriggerTime(cronExpression:String):Option[Long] =
    getNextTriggerTime(cronExpression,ZonedDateTime.now)

  /**
    * 获取下一次执行时间
    * @param cronExpression cron表达式
    * @param timeAfter 起始时间。
    * @return 下一次执行时间
    */
  def getNextTriggerTime(cronExpression:String, timeAfter:Long):Option[Long] =
    getNextTriggerTime(cronExpression,ZonedDateTime.ofInstant(Instant.ofEpochMilli(timeAfter), zoneId))

  /**
    * 根据当前时间获取上一次执行时间
    * @param cronExpression  cron表达式
    * @param timeBefore 指定时间
    * @return 上一次执行时间
    */
  def getPreviousTriggerTime(cronExpression:String,timeBefore:Long):Option[Long] =
    getPreviousTriggerTime(cronExpression,ZonedDateTime.ofInstant(Instant.ofEpochMilli(timeBefore), zoneId))

  /**
    * 获取当前cron表达式的英文描述
    * @param cronExpression cron表达式
    * @return cron表达式的英文描述
    */
  def getDescribe(cronExpression:String):String =
    descriptor.describe(parser.parse(cronExpression))

  /**
    * 判断当前cron表达式是否合法
    * @param cronExpression cron表达式
    * @return true合法；false不合法
    */
  def isValid(cronExpression:String):Boolean = {
    try{
      parser.parse(cronExpression)
      true
    }catch{
      case ex:Exception =>
        false
    }
  }

}