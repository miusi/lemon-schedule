package com.gabry.job.db.proxy

import com.gabry.job.db.proxy.command.DatabaseCommand

/**
  * Created by gabry on 2018/5/14 13:35
  */

final case class DataAccessProxyException[T](source:DatabaseCommand[T],reason:Throwable) extends Throwable
