package com.gabry.job.quartz
import java.util.Date

/**
  * wrap msg with scheduledFireTime
  */
final case class MessageWithFireTime(msg: AnyRef, scheduledFireTime:Date)

final case class MessageRequireFireTime(msg: AnyRef)