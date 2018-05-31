package com.gabry.job.core.domain

import java.util.concurrent.TimeUnit

/**
  * Created by gabry on 2018/4/18 10:37
  * 作业的依赖
  * @param uid 该依赖实体UID
  * @param jobUid 当前作业UID
  * @param dependJobUid 依赖的作业UID
  * @param timeOffset 当前作业与依赖作业的时间偏移量
  * @param timeOffsetUnit 当前作业与依赖作业的时间偏移量单位
  */
final case class Dependency(
                             uid:UID,
                             jobUid:UID,
                             dependJobUid:UID,
                             timeOffset:Long,
                             timeOffsetUnit: TimeUnit) extends Entity {
  override def toString: String = s"Dependency(uid=$uid,dependJobUid=$dependJobUid," +
    s"timeOffset=$timeOffset,timeOffsetUnit=$timeOffsetUnit)"
}
