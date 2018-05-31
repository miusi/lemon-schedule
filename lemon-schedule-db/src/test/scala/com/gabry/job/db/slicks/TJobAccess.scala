package com.gabry.job.db.slicks

import java.util.concurrent.TimeUnit

import com.gabry.job.core.domain.Job
import com.gabry.job.db.slicks
import com.gabry.job.db.slicks.schema.Tables
import com.typesafe.config.ConfigFactory
import org.scalatest.{BeforeAndAfterAll, FunSuite}
import slick.jdbc.MySQLProfile.api._

import scala.concurrent.duration.FiniteDuration
import scala.concurrent.{Await, ExecutionContextExecutor}

/**
  * Created by gabry on 2018/4/12 14:24
  * 测试数据库相关的类
  */
class TJobAccess extends FunSuite with BeforeAndAfterAll{
  implicit lazy val executionContext: ExecutionContextExecutor = scala.concurrent.ExecutionContext.global
  val db = Database.forConfig("",ConfigFactory.load().getConfig("db.mysql"))
  val jobAccess = new SlickJobAccess(db)
  val scheduleNode = "3958164162305738376-node"
  val job:Tables.JobsRow = slicks.jobPo2Row(Job("0", "3958164162305738376-test","com.gabry.job.examples.TestTask","",0,TimeUnit.MINUTES))
    .copy(schedulerNode = Some(scheduleNode))
  val duration = FiniteDuration(3,TimeUnit.SECONDS)
  override def beforeAll(): Unit = {
    super.beforeAll()
  }

  override def afterAll(): Unit = {
    super.afterAll()
    jobAccess.delete(job)
    db.close()
  }
  test("JobAccess insert"){
    val insert = Await.result(jobAccess.insert(job),duration)
    assert(insert != null )
    assert(insert.name == job.name)
  }
  test("JobAccess select"){
    val select = Await.result(jobAccess.selectOne(job.name),duration)
    assert(select.isDefined)
    assert(select.get.name == job.name)
  }
  test("JobAccess update"){
    val updateClassName = "updateClassName"
    val old = Await.result(jobAccess.selectOne(job.name),duration)
    assert(old.isDefined)
    assert(old.get.className!=updateClassName)
    val update = Await.result(jobAccess.update(job.copy(className = updateClassName)),duration)
    assert(update > 0 )
    val newJob = Await.result(jobAccess.selectOne(job.name),duration)
    assert(newJob.isDefined)
    assert(newJob.get.className==updateClassName)
  }
  test("JobAccess selectJobsByScheduleNode"){
    jobAccess.selectJobsByScheduleNode(scheduleNode){ r =>
      assert(r.schedulerNode.isDefined && r.schedulerNode.get == scheduleNode)
    }
  }
  test("JobAccess insertOnDuplicateUpdate"){
    val insert1 = Await.result(jobAccess.insertOnDuplicateUpdate(job),duration)
    val insert2 = Await.result(jobAccess.insertOnDuplicateUpdate(job),duration)
    assert(insert1>0)
    assert(insert2>0)
  }
  test("JobAccess delete"){
    val delete = Await.result(jobAccess.delete(job),duration)
    assert(delete > 0 )
    val select = Await.result(jobAccess.selectOne(job.name),duration)
    assert(select.isEmpty)
  }
}
