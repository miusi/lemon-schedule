package com.gabry.job.db.factory

import java.util.concurrent.TimeUnit

import com.gabry.job.db.slicks.{SlickDependencyAccess, SlickJobAccess, SlickScheduleAccess, SlickTaskAccess}
import com.typesafe.config.ConfigFactory
import org.scalatest.{BeforeAndAfterAll, FunSuite}

import scala.concurrent.duration.FiniteDuration
import scala.concurrent.{Await, ExecutionContextExecutor}

/**
  * Created by gabry on 2018/5/7 9:57
  */
class TDatabaseFactory extends FunSuite with BeforeAndAfterAll{
  private implicit lazy val executionContext: ExecutionContextExecutor = scala.concurrent.ExecutionContext.global
  private val config = ConfigFactory.load()
  private val duration = FiniteDuration(3,TimeUnit.SECONDS)
  private val dataAccessFactory = DatabaseFactory.getDataAccessFactory(config).get
  override def beforeAll(): Unit = {
    super.beforeAll()
    dataAccessFactory.init()
  }
  override def afterAll(): Unit = {
    super.afterAll()
    dataAccessFactory.destroy()
  }
  test("TDatabaseFactory default jobAccess type"){
    val access = dataAccessFactory.getJobAccess
    assert(access.isInstanceOf[SlickJobAccess])
  }
  test("TDatabaseFactory jobAccess select"){
    val access = dataAccessFactory.getJobAccess
    assert(access.isInstanceOf[SlickJobAccess])

    val select = Await.result(access.selectOne("test"),duration)
    assert(select.isDefined)
    assert(select.get.name == "test")

  }
  test("TDatabaseFactory dependencyAccess type"){
    val access = dataAccessFactory.getDependencyAccess
    assert(access.isInstanceOf[SlickDependencyAccess])
  }
  test("TDatabaseFactory scheduleAccess type"){
    val access = dataAccessFactory.getScheduleAccess
    assert(access.isInstanceOf[SlickScheduleAccess])
  }
  test("TDatabaseFactory taskAccess type"){
    val access = dataAccessFactory.getTaskAccess
    assert(access.isInstanceOf[SlickTaskAccess])
  }
}
