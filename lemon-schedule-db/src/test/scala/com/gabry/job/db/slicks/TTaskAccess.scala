package com.gabry.job.db.slicks

import java.util.concurrent.TimeUnit

import com.gabry.job.core.domain.UID
import com.gabry.job.db.slicks.schema.Tables
import com.typesafe.config.ConfigFactory
import org.scalatest.{BeforeAndAfterAll, FunSuite}
import slick.jdbc.MySQLProfile.api._

import scala.concurrent.duration.FiniteDuration
import scala.concurrent.{Await, ExecutionContextExecutor}
/**
  * Created by gabry on 2018/4/12 16:32
  * 测试TaskAccess类
  */
class TTaskAccess extends FunSuite with BeforeAndAfterAll{
  implicit lazy val executionContext: ExecutionContextExecutor = scala.concurrent.ExecutionContext.global
  val db = Database.forConfig("",ConfigFactory.load().getConfig("db.mysql"))
  val taskAccess = new SlickTaskAccess(db)
  var jobIdAndTriggerTime: (UID, Long) = ("999",1523497644627L)
  val taskTrackerNode = "3958164162305738376-node"
  val taskPo:Tables.TasksRow = Tables.TasksRow(-1,jobIdAndTriggerTime._1,jobIdAndTriggerTime._1,"-1",1,taskTrackerNode,"TEST",jobIdAndTriggerTime._2,Some("test"),null)
  val duration = FiniteDuration(3,TimeUnit.SECONDS)

  override def beforeAll(): Unit = {
    super.beforeAll()
  }

  override def afterAll(): Unit = {
    super.afterAll()
    db.close()
  }
  test("TaskAccess insert, select,delete"){
    val insert = Await.result(taskAccess.insert(taskPo) ,duration)
    assert(insert!=null)
    assert(insert.state==taskPo.state)
    val select = Await.result(taskAccess.selectOne(insert.uid),duration)
    assert(select.isDefined)
    assert(select.get.state==insert.state)
    val delete = Await.result(taskAccess.delete(insert),duration)
    assert(delete>0)
    val select1 = Await.result(taskAccess.selectOne(insert.uid),duration)
    assert(select1.isEmpty)
  }
  test("TaskAccess insertOnDuplicateUpdate"){
    val insert = Await.result(taskAccess.insertOnDuplicateUpdate(taskPo) ,duration)
    assert(insert==0)
  }
}
