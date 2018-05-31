package com.gabry.job.db.slicks

import java.util.concurrent.TimeUnit

import com.gabry.job.core.domain.UID
import com.gabry.job.core.po.DependencyPo
import com.gabry.job.core.tools.UIDGenerator
import com.gabry.job.db.slicks.schema.Tables
import com.gabry.job.utils.Utils
import com.typesafe.config.ConfigFactory
import org.scalatest.{BeforeAndAfterAll, FunSuite}
import slick.jdbc.MySQLProfile.api._

import scala.concurrent.duration.FiniteDuration
import scala.concurrent.{Await, ExecutionContextExecutor}
/**
  * Created by gabry on 2018/4/19 9:19
  */
class TDependencyAccess extends FunSuite with BeforeAndAfterAll{
  implicit lazy val executionContext: ExecutionContextExecutor = scala.concurrent.ExecutionContext.global
  val db = Database.forConfig("",ConfigFactory.load().getConfig("db.mysql"))
  val dependencyAccess = new SlickDependencyAccess(db)
  val scheduleAccess = new SlickScheduleAccess(db)
  val duration = FiniteDuration(3,TimeUnit.SECONDS)
  val dependencyPo = DependencyPo("0","1","2",1,TimeUnit.MINUTES,1*60*1000,null)
  var jobIdAndTriggerTime: (UID, Long) = ("1",1523497644627L)
  //  case class SchedulesRow(id: Long, jobUid: Long, priority: Int,
  // retryTimes: Int, dispatched: Boolean = false, triggerTime: Long,
  // scheduleNode: String, scheduleTime: Long, succeed: Boolean = false,
  // dataTime: Long, updateTime: java.sql.Timestamp)

  val schedulePo:Tables.SchedulesRow = Tables.SchedulesRow(0,UIDGenerator.globalUIDGenerator.nextUID(),jobIdAndTriggerTime._1,2,3,false,
    jobIdAndTriggerTime._2,"3958164162305738376-node",123,false,
    Utils.calcPostOffsetTime(jobIdAndTriggerTime._2,0,TimeUnit.MINUTES),null)
  def truncateTable():Unit = {
    Await.result(db.run(Tables.Dependencies.schema.truncate),duration)
    Await.result(db.run(Tables.Schedules.schema.truncate),duration)
  }
  override def beforeAll(): Unit = {
    super.beforeAll()
  }

  override def afterAll(): Unit = {
    super.afterAll()
    db.close()
  }
  test("DependencyAccess insert"){
    truncateTable()
    val insert = Await.result(dependencyAccess.insert(dependencyPo),duration)
    assert(insert != null )
    assert(insert.rowId != 0)
    assert(insert.timeOffsetUnit == dependencyPo.timeOffsetUnit)

  }
  test("DependencyAccess select"){
    truncateTable()

    val insert = Await.result(dependencyAccess.insert(dependencyPo),duration)
    assert(insert != null )
    assert(insert.rowId != 0)
    val select = Await.result(dependencyAccess.selectOne(insert.uid),duration)
    assert(select.isDefined)
    assert(select.get.rowId == insert.rowId)
    assert(select.get.timeOffsetUnit == insert.timeOffsetUnit)

  }
  test("DependencyAccess update"){
    truncateTable()

    val insert = Await.result(dependencyAccess.insert(dependencyPo),duration)
    assert(insert != null )
    assert(insert.rowId != 0)

    val updateOffsetUnit = TimeUnit.HOURS
    val old = Await.result(dependencyAccess.selectOne(insert.uid),duration)
    assert(old.isDefined)
    assert(old.get.timeOffsetUnit!=updateOffsetUnit)

    val update = Await.result(dependencyAccess.update(insert.copy(timeOffsetUnit = updateOffsetUnit)),duration)
    assert(update > 0 )
    val newJob = Await.result(dependencyAccess.selectOne(insert.uid),duration)
    assert(newJob.isDefined)
    assert(newJob.get.timeOffsetUnit==updateOffsetUnit)
  }
  test("DependencyAccess insertOnDuplicateUpdate"){
    truncateTable()

    val insert1 = Await.result(dependencyAccess.insertOnDuplicateUpdate(dependencyPo.copy(timeOffset = -1)),duration)
    val insert2 = Await.result(dependencyAccess.insertOnDuplicateUpdate(dependencyPo),duration)
    assert(insert1>0)
    assert(insert2>0)
  }
  test("DependencyAccess insertMany"){
    truncateTable()

    val dependency = Array(dependencyPo,dependencyPo.copy(timeOffsetUnit = TimeUnit.HOURS,dependJobUid="1111"))
    val result = Await.result(dependencyAccess.insertMany(dependency),duration)
    assert(result.isDefined)
    assert(result.get>0)
  }
  test("DependencyAccess selectSucceedState both suc"){
    truncateTable()

    val dependency = Array(dependencyPo.copy(dependJobUid = "1",timeOffset = -1, timeOffsetMilliSec = -1*60*1000)
      ,dependencyPo.copy(dependJobUid = "2",timeOffset = -1, timeOffsetMilliSec = -1*60*1000))

    val insertDependency = Await.result(dependencyAccess.insertMany(dependency),duration)

    assert(insertDependency.isDefined)
    assert(insertDependency.get==2)

    val insertSchPo1 = Await.result(scheduleAccess.insertOnDuplicateUpdate(schedulePo.copy(jobUid = "1",succeed = true,dataTime = jobIdAndTriggerTime._2 -60*1000)),duration)
    assert(insertSchPo1==1)
    val insertSchPo2 = Await.result(scheduleAccess.insertOnDuplicateUpdate(schedulePo.copy(jobUid = "2",succeed = true,dataTime = jobIdAndTriggerTime._2 -60*1000)),duration)
    assert(insertSchPo2==1)
    val unSuccessState = Await.result(dependencyAccess.selectSucceedState("1",jobIdAndTriggerTime._2),duration)
    assert(unSuccessState)

  }
  test("DependencyAccess selectSucceedState only one suc"){
    truncateTable()
    val dependency = Array(dependencyPo.copy(dependJobUid = "1",timeOffset = -1, timeOffsetMilliSec = -1*60*1000)
      ,dependencyPo.copy(dependJobUid = "2",timeOffset = -1, timeOffsetMilliSec = -1*60*1000))

    val insertDependency = Await.result(dependencyAccess.insertMany(dependency),duration)

    assert(insertDependency.isDefined)
    assert(insertDependency.get==2)
    val insertSchPo1 = Await.result(scheduleAccess.insertOnDuplicateUpdate(schedulePo.copy(jobUid = "1",succeed = false,dataTime = jobIdAndTriggerTime._2 -60*1000)),duration)
    assert(insertSchPo1==1)
    val insertSchPo2 = Await.result(scheduleAccess.insertOnDuplicateUpdate(schedulePo.copy(jobUid = "2",succeed = true,dataTime = jobIdAndTriggerTime._2 -60*1000)),duration)
    assert(insertSchPo2==1)
    val unSuccessState = Await.result(dependencyAccess.selectSucceedState("1",jobIdAndTriggerTime._2),duration)
    assert(!unSuccessState)
  }
  test("DependencyAccess selectSucceedState only one match"){
    truncateTable()
    val dependency = Array(dependencyPo.copy(dependJobUid = "1",timeOffset = -1, timeOffsetMilliSec = -1*60*1000)
      ,dependencyPo.copy(dependJobUid = "2",timeOffset = -1, timeOffsetMilliSec = -1*60*1000))

    val insertDependency = Await.result(dependencyAccess.insertMany(dependency),duration)

    assert(insertDependency.isDefined)
    assert(insertDependency.get==2)
    val insertSchPo1 = Await.result(scheduleAccess.insertOnDuplicateUpdate(schedulePo.copy(jobUid = "1",succeed = true,dataTime = jobIdAndTriggerTime._2 -60*1000)),duration)
    assert(insertSchPo1>0)
    val unSuccessState = Await.result(dependencyAccess.selectSucceedState("1",jobIdAndTriggerTime._2),duration)
    assert(!unSuccessState)

  }
  test("DependencyAccess selectSucceedState with no dependency"){
    truncateTable()
    val insertSchPo1 = Await.result(scheduleAccess.insertOnDuplicateUpdate(schedulePo.copy(jobUid = "1",succeed = true,dataTime = jobIdAndTriggerTime._2 -60*1000)),duration)
    assert(insertSchPo1>0)
    val unSuccessState = Await.result(dependencyAccess.selectSucceedState("1",jobIdAndTriggerTime._2),duration)
    assert(unSuccessState)
  }
}
