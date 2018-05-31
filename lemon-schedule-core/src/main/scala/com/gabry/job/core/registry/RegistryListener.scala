package com.gabry.job.core.registry

import com.gabry.job.core.domain.Node
import com.gabry.job.core.registry.RegistryEvent.RegistryEvent

/**
  * Created by gabry on 2018/4/23 11:16
  * 注册中心监听器
  */
trait RegistryListener {
  // TODO: 2018年4月23日17:52:10 优化改接口，尽量的功能集中，高内聚
  // 可以监听，某种类型的节点、某个IP的节点、某个role的节点、某个具体节点

  /**
    * 检测当前节点的事件是否满足监听条件
    * 满足监听条件的会调用onEvent函数
    * @param node 事件对应的节点
    * @param event 发生的事件
    * @return true满足监听条件
    */
  def filter(node:Node,event:RegistryEvent):Boolean
  /**
    * 事件回调函数
    * @param node 对应的节点
    * @param event 发生的事件
    */
  def onEvent(node:Node,event:RegistryEvent):Unit
}
