package com.gabry.job.db.access

import com.gabry.job.core.domain.UID
import com.gabry.job.core.po.SchedulePo

import scala.concurrent.{ExecutionContext, Future}

/**
  * Created by gabry on 2018/3/30 14:24
  * 与作业执行计划表存储相关的接口
  */
trait ScheduleAccess extends DataAccess[(UID,Long),SchedulePo]{
  /**
    * 根据scheduleNode、triggerTime选择待调度的任务
    * @param jobUid 作业ID号
    * @param scheduleNode 调度器节点
    * @param triggerTime 触发时间
    * @param block 处理结果数据的block
    * @param maxNum 一次调用最多返回的记录数
    */
  def selectUnDispatchSchedule(jobUid:UID, scheduleNode:String, triggerTime:Long, maxNum:Int)(block: SchedulePo=>Unit)(implicit global: ExecutionContext):Unit

  /**
    * 将当前计划设置为已调度
    * @param scheduleUidd 计划ID
    * @param dispatched 执行计划调度状态
    */
  def setDispatched(scheduleUidd:UID, dispatched:Boolean)(implicit global: ExecutionContext):Future[Int]

  /**
    * 将当前计划设置为成功
    * @param scheduleUidd 计划ID
    * @param succeed 计划成功标志
    * @return 更新的行数
    */
  def setSucceed(scheduleUidd:UID, succeed:Boolean)(implicit global: ExecutionContext):Future[Int]

}