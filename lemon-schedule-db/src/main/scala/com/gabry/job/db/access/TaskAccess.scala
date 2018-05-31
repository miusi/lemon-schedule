package com.gabry.job.db.access

import com.gabry.job.core.domain.UID
import com.gabry.job.core.po.TaskPo

import scala.concurrent.{ExecutionContext, Future}

/**
  * Created by gabry on 2018/4/10 17:04
  */
trait TaskAccess extends DataAccess[UID,TaskPo]{
  def batchInsert(batch:Array[TaskPo])(implicit global: ExecutionContext):Future[Int]
}
