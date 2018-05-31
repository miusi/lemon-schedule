package com.gabry.job.db.slicks

import java.util.concurrent.TimeUnit

import com.gabry.job.core.builder.JobBuilder
import com.typesafe.config.ConfigFactory
import org.scalatest.{BeforeAndAfterAll, FunSuite}
import slick.jdbc.MySQLProfile.api._

import scala.concurrent.duration.FiniteDuration
import scala.concurrent.{Await, ExecutionContextExecutor, Future}
/**
  * Created by gabry on 2018/4/12 16:48
  * 计算每秒插入的时间，仅仅计算性能，作为参考
  */
class TInsertTime extends FunSuite with BeforeAndAfterAll{
  implicit lazy val executionContext: ExecutionContextExecutor = scala.concurrent.ExecutionContext.global
  val db = Database.forConfig("",ConfigFactory.load().getConfig("db.mysql"))
  val jobAccess = new SlickJobAccess(db)
  val duration = FiniteDuration(3,TimeUnit.DAYS)

  override def beforeAll(): Unit = {
    super.beforeAll()
  }

  override def afterAll(): Unit = {
    super.afterAll()
    db.close()
  }
  test("InsertTime"){
    val recordNum = 10000
    val futures = 1 to recordNum map{ i =>

      val job = JobBuilder().withName(i.toString)
        .withClass("com.gabry.job.examples.TestTask")
        .withDataTimeOffset(0)
        .withDataTimeOffsetUnit(TimeUnit.MINUTES)
        .build()

      jobAccess.insert(job)
    }
    val start = System.currentTimeMillis()
    val all = Future.sequence(futures)
    Await.result(all,duration)
    val end = System.currentTimeMillis()
    println(s"插入 $recordNum 条数据，总耗时 ${end-start} 毫秒，平均 ${(end-start)/recordNum} 毫秒/条")
  }
}
