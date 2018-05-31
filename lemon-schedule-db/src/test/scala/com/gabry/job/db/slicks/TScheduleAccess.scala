package com.gabry.job.db.slicks

import java.util.concurrent.TimeUnit

import com.gabry.job.core.domain.UID
import com.gabry.job.core.po.SchedulePo
import com.gabry.job.db.slicks.schema.Tables
import com.gabry.job.utils.Utils
import com.typesafe.config.ConfigFactory
import org.scalatest.{BeforeAndAfterAll, FunSuite}
import slick.jdbc.MySQLProfile.api._

import scala.concurrent.duration.FiniteDuration
import scala.concurrent.{Await, ExecutionContextExecutor}
/**
  * Created by gabry on 2018/4/12 15:52
  * 测试ScheduleAccess类
  */
class TScheduleAccess extends FunSuite with BeforeAndAfterAll{
  implicit lazy val executionContext: ExecutionContextExecutor = scala.concurrent.ExecutionContext.global
  val db = Database.forConfig("",ConfigFactory.load().getConfig("db.mysql"))
  val scheduleAccess = new SlickScheduleAccess(db)
  val scheduleNode = "3958164162305738376-node"
  var jobIdAndTriggerTime: (UID, Long) = ("999",1523497644627L)

  val schedulePo:Tables.SchedulesRow = SchedulePo("0",jobIdAndTriggerTime._1,2,3,false,
    jobIdAndTriggerTime._2,scheduleNode,123,false,
    Utils.calcPostOffsetTime(jobIdAndTriggerTime._2,0,TimeUnit.MINUTES),null)
  val duration = FiniteDuration(3,TimeUnit.SECONDS)
  override def beforeAll(): Unit = {
    super.beforeAll()
  }

  override def afterAll(): Unit = {
    super.afterAll()
    Await.result(scheduleAccess.delete(schedulePo) ,duration)
    db.close()
  }
  test("ScheduleAccess insert"){
    val insert = Await.result(scheduleAccess.insert(schedulePo.copy(jobUid = schedulePo.jobUid)) ,duration)
    assert(insert != null )
  }
  test("ScheduleAccess insertOnDuplicateUpdate"){
    val insert1 = Await.result(scheduleAccess.insertOnDuplicateUpdate(schedulePo),duration)
    val insert2 = Await.result(scheduleAccess.insertOnDuplicateUpdate(schedulePo),duration)
    assert(insert1 > 0 )
    assert(insert2 > 0 )
  }

  test("ScheduleAccess select setDispatched"){
    val select = Await.result(scheduleAccess.selectOne(jobIdAndTriggerTime),duration)
    assert(select.isDefined)
    assert(select.get.jobUid == jobIdAndTriggerTime._1 && select.get.triggerTime == jobIdAndTriggerTime._2)
    val update = Await.result(scheduleAccess.setDispatched(select.get.uid,true),duration)
    assert(update > 0 )

    val select1 = Await.result(scheduleAccess.selectOne(jobIdAndTriggerTime),duration)
    assert(select1.isDefined)
    assert(select1.get.dispatched)
  }
  test("ScheduleAccess update"){
    val updateScheduleNode = "updateScheduleNode"
    val old = Await.result(scheduleAccess.selectOne(jobIdAndTriggerTime),duration)
    assert(old.isDefined)
    assert(old.get.scheduleNode!=updateScheduleNode)

    val update = Await.result(scheduleAccess.update(schedulePo.copy(scheduleNode = updateScheduleNode)),duration)
    assert(update > 0 )
    val newJob = Await.result(scheduleAccess.selectOne(jobIdAndTriggerTime),duration)
    assert(newJob.isDefined)
    assert(newJob.get.scheduleNode == updateScheduleNode)
  }
  test("ScheduleAccess selectUnDispatchSchedule"){
    scheduleAccess.selectUnDispatchSchedule("1",scheduleNode,jobIdAndTriggerTime._2+30,2){ r=>
      assert(!r.dispatched)
    }
  }
}
