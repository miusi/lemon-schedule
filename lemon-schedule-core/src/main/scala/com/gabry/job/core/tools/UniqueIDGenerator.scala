package com.gabry.job.core.tools

import java.util.UUID

import com.gabry.job.core.domain.UID

/**
  * Created by gabry on 2018/5/18 10:07
  * UID生成器接口
  */
trait UniqueIDGenerator {
  def nextUID():UID
}
object UIDGenerator{
  val globalUIDGenerator:UniqueIDGenerator = new UIDGenerator
}
class UIDGenerator extends UniqueIDGenerator{
  override def nextUID(): UID = UUID.randomUUID().toString
}
