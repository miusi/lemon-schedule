package com.gabry.job.utils

import org.scalatest.FunSuite

/**
  * Created by gabry on 2018/4/12 10:46
  * 测试CronGenerator类
  */
class TCronGenerator extends FunSuite{
  test("CronGenerator isValid"){
    assert(CronGenerator.isValid("0/1 * * * *"))
    assert(!CronGenerator.isValid("abcdef"))
  }
  test("CronGenerator getDescribe"){
    assert(CronGenerator.getDescribe("0/1 * * * *")!="")
    intercept[IllegalArgumentException]{
      CronGenerator.getDescribe("abcdef")
    }
  }
  test("CronGenerator getNextTriggerTime"){
    val minutesCron = "0/1 * * * *"
    val start = System.currentTimeMillis()
    val next1 = CronGenerator.getNextTriggerTime(minutesCron,start)
    val next2 = CronGenerator.getNextTriggerTime(minutesCron,next1.get)
    assert(next2.get-next1.get==60*1000)
    assert(next1.get-start<60*1000)
  }
  test("CronGenerator getPreviousTriggerTime"){
    val minutesCron = "0/1 * * * *"
    val start = System.currentTimeMillis()
    val next1 = CronGenerator.getNextTriggerTime(minutesCron,start)
    val next2 = CronGenerator.getNextTriggerTime(minutesCron,next1.get)
    val prev = CronGenerator.getPreviousTriggerTime(minutesCron,next2.get)
    assert(prev.get==next1.get)
  }
  test("CronGenerator getNextTriggerTime for now"){
    val minutesCron = "0/1 * * * *"
    val start = System.currentTimeMillis()
    val next = CronGenerator.getNextTriggerTime(minutesCron)
    assert(next.get-start<60*1000)
  }
  test("CronGenerator None"){
    val minutesCron = "0/1 *"
    val start = System.currentTimeMillis()
    val next1 = CronGenerator.getNextTriggerTime(minutesCron)
    val next2 = CronGenerator.getNextTriggerTime(minutesCron,start)
    val prev = CronGenerator.getPreviousTriggerTime(minutesCron,start)
    assert(next1.isEmpty && next2.isEmpty && prev.isEmpty)
  }
}
