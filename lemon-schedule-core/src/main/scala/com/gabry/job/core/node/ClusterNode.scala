package com.gabry.job.core.node

import akka.actor.{Actor, ActorLogging, ActorPath, Props}
import akka.cluster.ClusterEvent._
import akka.cluster.metrics.ClusterMetricsChanged
import akka.cluster.{Cluster, Member}
import com.gabry.job.core.constant.Constants
import com.gabry.job.core.domain.Node
import com.gabry.job.core.registry.{Registry, RegistryFactory}
import com.typesafe.config.Config

/**
  * Created by gabry on 2018/3/23 15:15
  * ClusterNode抽象节点类
  */
trait ClusterNodeProps{
  def props(args: Any*):Props
  def props:Props
  val daemonName:String
}

/**
  * ClusterNode抽象节点类
  */
abstract class ClusterNode extends Actor with ActorLogging{
  /**
    * 当前cluster
    */
  protected val cluster = Cluster(context.system)
  /**
    * 当前system配置
    */
  protected val config: Config = context.system.settings.config
  /**
    * 注册中心
    */
  protected var registry:Registry = _

  /**
    * 当前集群名称
    */
  protected val clusterName: String = config.getString(Constants.CLUSTER_NAME_CONFIG_PATH)

  /**
    * 带有Address信息的ActorPath
    */
  protected var selfAnchor:String = _
  /**
    * 集群当前节点信息
    */
  protected var currentMembers:Array[Member] = Array.empty
  /**
    * 初始化Registry
    */
  private def initRegistry(): Unit ={
    if( registry == null ){
      registry = RegistryFactory.getRegistry(config).get
      registry.connect()
    }
  }
  private def joinCluster(): Unit ={
    selfAnchor = self.path.toStringWithAddress(cluster.selfAddress)
    log.debug(s"Node actorAnchor = $selfAnchor")

    val seeds = registry.getNodesByType(Constants.ROLE_SEED_NAME).map(seed=>ActorPath.fromString(seed.anchor).address)

    if(seeds.isEmpty){
      log.warning(s"Current cluster is empty ,now start first seed node and join self ${cluster.selfAddress}")
      cluster.join(cluster.selfAddress)
    }else{
      log.info(s"Current cluster is not empty ,now join in [${seeds.mkString(",")}]")
      cluster.joinSeedNodes( seeds.toList )
    }

    log.info(s"Current node roles = ${cluster.selfRoles}")
    cluster.selfRoles.foreach{ role =>
      registry.registerNode(Node(role,selfAnchor))
    }
  }
  override def preStart(): Unit = {
    initRegistry()
    joinCluster()
    cluster.subscribe(self, initialStateMode = InitialStateAsSnapshot,
      classOf[MemberEvent]
      ,classOf[UnreachableMember]
      ,classOf[ReachableMember]
      ,classOf[LeaderChanged]
      ,classOf[RoleLeaderChanged])
    log.info(s"Node [${this.getClass}] started at $self")
  }

  override def postStop(): Unit = {
    cluster.selfRoles.foreach{ role=>
      registry.unRegisterNode(Node(role,selfAnchor))
    }
    registry.disConnect()
    cluster.unsubscribe(self)
    log.warning(s"Node [${this.getClass}] at $self stopped")
  }

  /**
    * 处理用户自定义消息
    */
  def userDefineEventReceive:Receive

  /**
    * 节点加入时调用
    * @param member 加入集群的节点
    */
  def register(member:Member):Unit

  /**
    * 节点离开时调用
    * @param member 离开集群的节点
    */

  def unRegister(member:Member):Unit

  /**
    * 处理集群的Member事件
    */
  private final def memberEventReceive:Receive = {
    /**
      * Member status changed to Joining.
      */
    case MemberJoined(member)=>
      log.info(s"Member joined $member")
    case MemberWeaklyUp(member) =>
      log.info(s"Member weaklyUp $member")

    /**
      * Member status changed to `MemberStatus.Exiting` and will be removed
      * when all members have seen the `Exiting` status.
      */
    case MemberExited(member)=>
      log.info(s"Member exited $member")
    /**
      * Member status changed to Leaving.
      */
    case MemberLeft(member)=>
      log.info(s"Member left $member")
    /**
      * A member is considered as reachable by the failure detector
      * after having been unreachable.
      * @see [[UnreachableMember]]
      */
    case ReachableMember(member)=>
      log.info(s"Member reachable $member")

    /**
      * Member status changed to Up.
      */
    case MemberUp(member) =>
      log.info(s"Member is up,member =$member,sender ${sender()},self = $self,selfWithAddress = ${self.path.toStringWithAddress(cluster.selfAddress)}")
      currentMembers = currentMembers :+ member
      register(member)

    /**
      * A member is considered as unreachable by the failure detector.
      */
    case UnreachableMember(member) =>
      log.info(s"Member unreachable $member ,now down it")
      cluster.down(member.address)
      currentMembers = currentMembers.filter(_!=member)
      unRegister(member)
    /**
      * Member completely removed from the cluster.
      * When `previousStatus` is `MemberStatus.Down` the node was removed
      * after being detected as unreachable and downed.
      * When `previousStatus` is `MemberStatus.Exiting` the node was removed
      * after graceful leaving and exiting.
      */
    case MemberRemoved(member, previousStatus) =>
      log.info(s"Member removed $member ,previousStatus is $previousStatus")
  }

  /**
    * 处理集群领域的事件
    */
  private final def clusterDomainEventReceive:Actor.Receive = {
    /**
      * Leader of the cluster data center of this node changed. Published when the state change
      * is first seen on a node.
      */
    case LeaderChanged(leader) =>
      log.info(s"Cluster leader is changed $leader")
    /**
      * First member (leader) of the members within a role set (in the same data center as this node,
      * if data centers are used) changed.
      * Published when the state change is first seen on a node.
      */
    case RoleLeaderChanged(role,leader) =>
      log.info(s"Cluster role leader is changed $leader,role $role")

    /**
      * This event is published when the cluster node is shutting down,
      * before the final [[MemberRemoved]] events are published.
      */
    case ClusterShuttingDown =>
      log.info(s"Cluster shutting down")
    /**
      * Current snapshot state of the cluster. Sent to new subscriber.
      *

    val members: immutable.SortedSet[Member],
    val unreachable: Set[Member],
    val seenBy: Set[Address],
    val leader: Option[Address],
    val roleLeaderMap: Map[String, Option[Address]],
      */
    case CurrentClusterState(members,unreachable,seenBy,leader,roleLeaderMap) =>
      log.info(s"CurrentClusterState members = ${members.mkString(",")},unreachable = ${unreachable.mkString(",")},seenBy = ${seenBy.mkString(",")}," +
        s"leader = $leader,roleLeaderMap = ${roleLeaderMap.mkString(",")}")
    // members filter(_.status == MemberStatus.Up) foreach register
  }

  /**
    * 处理度量事件
    */
  protected def metricsEventReceive:Actor.Receive = {
    case ClusterMetricsChanged(nodeMetrics)=>
      log.info(s"Cluster metricsChanged ${nodeMetrics.mkString(",")}")
  }
  protected def unknownEventReceive:Actor.Receive = {
    case unknownMessage:Any =>
      log.error(s"receive unknown message $unknownMessage,type is ${unknownMessage.getClass}")
  }
  /**
    * 全局消息处理入口
    */
  override def receive: Receive = memberEventReceive orElse
    clusterDomainEventReceive orElse
    metricsEventReceive orElse
    userDefineEventReceive orElse
    unknownEventReceive

}
