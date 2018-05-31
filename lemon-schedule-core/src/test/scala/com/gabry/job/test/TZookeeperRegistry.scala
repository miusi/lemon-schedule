package com.gabry.job.test

import com.gabry.job.core.domain.Node
import com.gabry.job.core.registry.RegistryEvent.RegistryEvent
import com.gabry.job.core.registry.{AbstractRegistry, RegistryFactory, RegistryListener}
import com.typesafe.config.{Config, ConfigFactory}
import org.scalatest.{BeforeAndAfterAll, FunSuite}

/**
  * Created by gabry on 2018/3/26 10:36
  * 编写测试方法类。具体参见 https://blog.csdn.net/zero007/article/details/50804094
  */
class TZookeeperRegistry extends FunSuite with BeforeAndAfterAll {
  var registry:AbstractRegistry = _
  val config: Config = ConfigFactory.parseString(zkConfigStr)
  override def beforeAll(): Unit = {
    super.beforeAll()
    registry = RegistryFactory.getRegistry(config).get
    registry.connect()
  }

  override def afterAll(): Unit = {
    super.afterAll()
    clearAllNode()
    registry.disConnect()
  }
  def clearAllNode():Unit = {
    registry.getAllNodes.foreach{ node =>
      registry.unRegisterNode(node)
    }
  }
  test("ZkRegistry connected"){
    assert(registry.isConnected)
  }
  test("ZkRegistry register"){
    clearAllNode()
    registry.registerNode(Node("test","testNode"))
    registry.registerNode(Node("test","testNode1"))
    assert(registry.getNodesByType("test").length==2)
    assert(registry.getNodesByType("test").exists(_.anchor == "testNode"))
    assert(registry.getNodesByType("test").exists(_.anchor == "testNode1"))
  }
  test("ZkRegistry unRegister"){
    clearAllNode()
    registry.registerNode(Node("test","testNode"))
    registry.unRegisterNode(Node("test","testNode"))
    assert(registry.getNodesByType("test").isEmpty)
  }
  test("ZkRegistry getNodes"){
    clearAllNode()
    0 until 3 foreach{ i =>
      registry.registerNode(Node("test",i.toString))
    }
    assert(registry.getNodesByType("test").length==3)
  }
  test("ZkRegistry getAllNodes"){
    clearAllNode()
    0 until 3 foreach{ i =>
      registry.registerNode(Node(s"test[$i]",i.toString))
    }
    val allNodes = registry.getAllNodes
    assert(allNodes.length == 3)
    0 until 3 foreach { i =>
      assert(allNodes(i).nodeType==s"test[$i]" && allNodes(i).anchor==i.toString)
    }
  }
  test("ZkRegistry listener"){
    clearAllNode()
    val regNode = Node("test","testNode")
    registry.subscribe(new RegistryListener{
      /**
      * 检测当前节点的事件是否满足监听条件
      * 满足监听条件的会调用onEvent函数
      *
      * @param node  事件对应的节点
      * @param event 发生的事件
      * @return true满足监听条件
      */
    override def filter(node: Node, event: RegistryEvent): Boolean = node == regNode

      /**
        * 事件回调函数
        *
        * @param node  对应的节点
        * @param event 发生的事件
        */
      override def onEvent(node: Node, event: RegistryEvent): Unit = {
        assert(regNode == node)
      }
    })
    registry.registerNode(regNode)
    Thread.sleep(1000)
  }
  def zkConfigStr = """registry{
                   |  type = "zookeeper"
                   |  zookeeper{
                   |    hosts = "dn1:2181,dn3:2181,dn4:2181"
                   |    exponential-backoff-retry {
                   |      base-sleep-timeMs = 1000
                   |      max-retries = 3
                   |    }
                   |    root-path = "/lemon-schedule"
                   |  }
                   |}
                   """.stripMargin
}
