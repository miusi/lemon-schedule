package com.gabry.job.core

import com.gabry.job.core.po.SchedulePo

/**
  * Created by gabry on 2018/4/26 15:47
  */
package object domain {
  /**
    * 定义实体ID字段类型
    */
  type UID = String
  implicit def schedulePo2Schedule(schedulePo: SchedulePo):Schedule = Schedule(schedulePo.uid,schedulePo.jobUid,schedulePo.priority,schedulePo.retryTimes,
    schedulePo.dispatched,schedulePo.triggerTime,schedulePo.scheduleNode,schedulePo.scheduleTime)

}
