package com.gabry.job.core.builder

import java.util.concurrent.TimeUnit

import com.gabry.job.core.domain.{Dependency, UID}
import com.gabry.job.core.tools.UIDGenerator

/**
  * Created by gabry on 2018/4/24 9:45
  * Dependency创建器
  */
object DependencyBuilder {
  def apply(): DependencyBuilder = new DependencyBuilder()
}
class DependencyBuilder extends Builder[Dependency]{
  private var jobUid:UID = _
  private var dependJobId:UID = _
  private var timeOffset:Long = _
  private var timeOffsetUnit:TimeUnit = _
  def withJobUid(jobUid:UID):this.type = {
    this.jobUid = jobUid
    this
  }
  def withDependJobId(dependJobId:UID):this.type ={
    this.dependJobId = dependJobId
    this
  }
  def withTimeOffset(timeOffset:Long):this.type = {
    this.timeOffset = timeOffset
    this
  }
  def withTimeOffsetUnit(timeOffsetUnit:TimeUnit):this.type = {
    this.timeOffsetUnit = timeOffsetUnit
    this
  }
  override def build(): Dependency = Dependency(UIDGenerator.globalUIDGenerator.nextUID(),jobUid,dependJobId,timeOffset,timeOffsetUnit)
}