package com.gabry.job.db.slicks

import com.gabry.job.db.access._
import com.typesafe.config.Config
import slick.jdbc.MySQLProfile.api._
/**
  * Created by gabry on 2018/5/7 13:38
  * 获取四个SlickDataAccess实例
  */
class SlickDataAccessFactory(config:Config) extends DataAccessFactory{

  private final var database:Database = _
  private final var jobAccess:JobAccess = _
  private final var dependencyAccess:DependencyAccess = _
  private final var scheduleAccess:ScheduleAccess = _
  private final var taskAccess:TaskAccess = _

  override def getJobAccess: JobAccess = jobAccess

  override def getDependencyAccess: DependencyAccess = dependencyAccess

  override def getScheduleAccess: ScheduleAccess = scheduleAccess

  override def getTaskAccess: TaskAccess = taskAccess

  override def init(): Unit = {
    database = Database.forConfig("",config)
    jobAccess = new SlickJobAccess(database)
    dependencyAccess = new SlickDependencyAccess(database)
    scheduleAccess = new SlickScheduleAccess(database)
    taskAccess = new SlickTaskAccess(database)
  }

  override def destroy(): Unit = {
    // 不要判断database是否null，不要问为什么
    database.close()
    database = null
    jobAccess = null
    dependencyAccess = null
    scheduleAccess = null
    taskAccess = null
  }
}
