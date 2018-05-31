package com.gabry.job.core.daemon

import akka.actor.{ActorRef, ActorSystem}
import com.gabry.job.core.node.ClusterNodeProps
import com.typesafe.config.ConfigFactory

import scala.util.Try

/**
  * Created by gabry on 2018/5/9 14:48
  */
final case class Daemon(system:ActorSystem,actor:ActorRef)

object DaemonCreator {
  private val defaultPort = 0
  def createDaemon(nodeProps:ClusterNodeProps,port:Option[Int]):Try[Daemon] = Try{
    val config = ConfigFactory.parseString(s"akka.remote.netty.tcp.port=${port.getOrElse(defaultPort)}")
      .withFallback(ConfigFactory.load())
    val clusterName = config.getString("clusterNode.cluster-name")
    val system = ActorSystem(clusterName, config)
    val daemonActor = system.actorOf(nodeProps.props,nodeProps.daemonName)
    Daemon(system,daemonActor)
  }
}
