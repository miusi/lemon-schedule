package com.gabry.job.utils

import java.util.concurrent.TimeUnit
import java.util.{Calendar, Date}

import org.scalatest.FunSuite

import scala.util.Random

/**
  * Created by gabry on 2018/4/12 9:44
  * 测试Utils类
  */
class TUtils extends FunSuite {
  test("formatAliveTime days"){
    val start = 1523497644627L

    0 until 2 foreach{ days =>
      0 until 24 foreach{ hours =>
        val hoursTime = hours * 60 * 60 * 1000
        val end = start + days * 24 * 60 * 60 * 1000 + hoursTime
        val alive = Utils.formatAliveTime(start,end)
        assert(alive.startsWith(s"$days days"))
      }
    }
  }
  test("formatAliveTime hours"){
    val start = 1523497644627L
    0 until 24 foreach{ hours =>
      val hoursTime = hours * 60 * 60 * 1000
      val end = start + hoursTime
      val alive = Utils.formatAliveTime(start,end)
      assert(alive.startsWith(s"0 days $hours hours"))
    }
  }
  test("formatAliveTime minutes"){
    val start = 1523497644627L
    0 until 60 foreach{ minutes =>
      val hoursTime = minutes * 60 * 1000
      val end = start + hoursTime
      val alive = Utils.formatAliveTime(start,end)
      assert(alive.startsWith(s"0 days 0 hours $minutes minutes"))
    }
  }

  test("formatAliveTime seconds"){
    val start = 1523497644627L
    0 until 60 foreach{ seconds =>
      val hoursTime = seconds * 1000
      val end = start + hoursTime
      val alive = Utils.formatAliveTime(start,end)
      assert(alive.startsWith(s"0 days 0 hours 0 minutes $seconds seconds"))
    }
  }

  test("formatAliveTime milliseconds"){
    val start = 1523497644627L
    0 until 1000 foreach{ milliseconds =>
      val hoursTime = milliseconds
      val end = start + hoursTime
      val alive = Utils.formatAliveTime(start,end)
      assert(alive.startsWith(s"0 days 0 hours 0 minutes 0 seconds $milliseconds milliseconds"))
    }
  }

  test("formatAliveTime random time"){
    val start = 1523497644627L
    val days = Random.nextInt(10)
    val hours = Random.nextInt(24)
    val minutes =  Random.nextInt(60)
    val seconds =  Random.nextInt(60)
    val milliseconds =  Random.nextInt(1000)
    val end = start + days * 24*60*60*1000 + hours * 60*60*1000 + minutes*60*1000 + seconds * 1000 + milliseconds
    val alive = Utils.formatAliveTime(start,end)
    assert(alive == s"$days days $hours hours $minutes minutes $seconds seconds $milliseconds milliseconds")
  }
  test("formatDate date equal long"){
    val seconds = 1523497644627L
    val date = new Date(seconds)
    assert(Utils.formatDate(date)==Utils.formatDate(seconds))
  }
  test("formatDate format"){
    val date = new Date()
    val calendar = Calendar.getInstance()
    calendar.setTime(date)
    val year = calendar.get(Calendar.YEAR)
    val month = calendar.get(Calendar.MONTH)+1
    val day = calendar.get(Calendar.DAY_OF_MONTH)
    val hour = calendar.get(Calendar.HOUR_OF_DAY)
    val minutes = calendar.get(Calendar.MINUTE)
    val seconds = calendar.get(Calendar.SECOND)
    val milliseconds = calendar.get(Calendar.MILLISECOND)
    assert(Utils.formatDate(date)==f"$year%4d-$month%02d-$day%02d $hour%02d:$minutes%02d:$seconds%02d.$milliseconds%03d")
  }
  test("calcDataTime"){
    val triggerTime = 1523497644627L
    val randomOffset = Random.nextInt(90)
    val dataTimeMilliSec = Utils.calcPostOffsetTime(triggerTime,randomOffset,TimeUnit.MILLISECONDS)
    val dataTimeSec = Utils.calcPostOffsetTime(triggerTime,randomOffset,TimeUnit.SECONDS)
    val dataTimeMinute = Utils.calcPostOffsetTime(triggerTime,randomOffset,TimeUnit.MINUTES)
    val dataTimeHour = Utils.calcPostOffsetTime(triggerTime,randomOffset,TimeUnit.HOURS)
    val dataTimeDay = Utils.calcPostOffsetTime(triggerTime,randomOffset,TimeUnit.DAYS)

    assert(dataTimeMilliSec==triggerTime+randomOffset)
    assert(dataTimeSec==triggerTime+randomOffset*1000L)
    assert(dataTimeMinute==triggerTime+randomOffset*1000*60L)
    assert(dataTimeHour==triggerTime+randomOffset*1000*60*60L)
    assert(dataTimeDay==triggerTime+randomOffset*1000*60*60*24L)

  }
}
