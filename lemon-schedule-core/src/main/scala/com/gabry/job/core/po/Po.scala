package com.gabry.job.core.po

import com.gabry.job.core.domain.Entity

/**
  * Created by gabry on 2018/5/14 14:10
  */
trait Po extends Entity{
  def updateTime: java.sql.Timestamp
}
