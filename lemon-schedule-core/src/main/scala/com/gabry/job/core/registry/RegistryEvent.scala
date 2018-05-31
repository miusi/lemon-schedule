package com.gabry.job.core.registry

/**
  * Created by gabry on 2018/4/23 11:14
  * 注册中心事件
  * 注册一般指节点的注册，简单起见分为加入和离开两个事件
  */
object RegistryEvent extends Enumeration {
  type RegistryEvent = Value
  val JOIN = Value("JOIN")
  val LEAVE = Value("LEAVE")
}
