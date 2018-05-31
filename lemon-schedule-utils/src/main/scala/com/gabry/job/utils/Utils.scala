package com.gabry.job.utils

import java.sql.Timestamp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.concurrent.TimeUnit

import scala.collection.mutable.ArrayBuffer

/**
  * Created by gabry on 2018/3/26 15:17
  * 通用的工具类
  */
object Utils {
  private val SIMPLE_DATE_FORMAT_STRING = "yyyy-MM-dd HH:mm:ss.SSS"
  private lazy val simpleDateFormat = new SimpleDateFormat(SIMPLE_DATE_FORMAT_STRING)
  private lazy val classesField = {
    val field = classOf[ClassLoader].getDeclaredField("classes")
    field.setAccessible(true)
    field
  }
  /**
    * 打印当前的时间间隔
    * @param start 开始时间
    * @param end 终止时间
    * @return 格式化后的时间间隔
    */
  def formatAliveTime(start:Long,end:Long):String = {
    val millisDiff = end - start
    val days:Long= millisDiff/(1000*60*60*24)
    val hours:Long = millisDiff % (1000*60*60*24) / (1000*60*60)
    val min :Long = millisDiff % (1000*60*60*24) % (1000*60*60) / (1000*60)
    val sec :Long = millisDiff % (1000*60*60*24) % (1000*60*60) % (1000*60) / 1000
    val millis:Long = millisDiff % (1000*60*60*24) % (1000*60*60) % (1000*60) % 1000
    s"$days days $hours hours $min minutes $sec seconds $millis milliseconds"
  }
  /**
    * 将时间格式化成标准格式yyyy-MM-dd HH:mm:ss.SSS
    * @param date 待格式化的时间
    * @return 格式化后的时间
    */
  def formatDate(date:Timestamp):String = if(date!=null)simpleDateFormat.format(date) else simpleDateFormat.format(new Date())
  /**
    * 将时间格式化成标准格式yyyy-MM-dd HH:mm:ss.SSS
    * @param date 待格式化的时间
    * @return 格式化后的时间
    */
  def formatDate(date:Date):String = if(date!=null) simpleDateFormat.format(date) else simpleDateFormat.format(new Date())

  /**
    * 将时间格式化成标准格式yyyy-MM-dd HH:mm:ss.SSS
    * @param date 待格式化的时间
    * @return 格式化后的时间
    */
  def formatDate(date:Long):String = simpleDateFormat.format(new Date(date))

  /**
    * 计算偏移后的时间
    * @param baseTime 时间基线
    * @param offset 时间的偏移量
    * @param offsetUnit 时间的偏移量单位
    * @return 偏移后的时间
    */
  def calcPostOffsetTime(baseTime:Long, offset:Long, offsetUnit:TimeUnit):Long =
    baseTime + offsetUnit.toMillis(offset)

  /**
    * 计算偏移后的时间
    * @param baseTime 时间基线
    * @param offset 时间的偏移量
    * @param offsetUnit 时间的偏移量单位
    * @return 偏移后的时间
    */
  def calcPostOffsetTime(baseTime:Long, offset:Long, offsetUnit:String):Long =
    calcPostOffsetTime(baseTime,offset,TimeUnit.valueOf(offsetUnit))

  /**
    * 获取指定ClassLoader加载的class
    * @param classLoader 待查询的ClassLoader
    * @return 加载的Class列表
    */
  def getLoadedClass(classLoader: ClassLoader):Array[Class[_]] = {
    val loadedClass = ArrayBuffer.empty[Class[_]]

    val loadedClassEnum = classesField.get(classLoader).asInstanceOf[java.util.Vector[Class[_]]].elements()

    while(loadedClassEnum.hasMoreElements){
      val nextElement = loadedClassEnum.nextElement()
      loadedClass.append(nextElement)
    }
    loadedClass.toArray
  }
}
