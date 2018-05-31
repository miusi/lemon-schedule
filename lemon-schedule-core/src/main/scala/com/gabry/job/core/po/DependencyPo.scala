package com.gabry.job.core.po

import java.util.concurrent.TimeUnit

import com.gabry.job.core.domain.UID

/**
  * Created by gabry on 2018/4/24 16:14
  */
case class DependencyPo(uid: UID,
                        jobUid: UID,
                        dependJobUid: UID,
                        timeOffset: Long,
                        timeOffsetUnit: TimeUnit,
                        timeOffsetMilliSec: Long,
                        updateTime: java.sql.Timestamp = null) extends Po{
  override def toString: String = s"DependencyPo(uid=$uid,jobUid=$jobUid,dependJobUid=$dependJobUid,timeOffset=$timeOffset,timeOffsetUnit=$timeOffsetUnit," +
    s"timeOffsetMilliSec=$timeOffsetMilliSec)"
}