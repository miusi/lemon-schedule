package com.gabry.job.db.access

/**
  * Created by gabry on 2018/5/7 13:35
  * DataAccess工厂类
  * 获取四个DataAccess实例
  */
trait DataAccessFactory {
  def init():Unit
  def getJobAccess:JobAccess
  def getDependencyAccess:DependencyAccess
  def getScheduleAccess:ScheduleAccess
  def getTaskAccess:TaskAccess
  def destroy():Unit
}