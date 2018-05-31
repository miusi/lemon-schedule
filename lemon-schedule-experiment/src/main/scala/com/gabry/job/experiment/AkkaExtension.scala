package com.gabry.job.experiment

import akka.actor.{ExtendedActorSystem, Extension, ExtensionId, ExtensionIdProvider}

/**
  * Created by gabry on 2018/5/15 10:17
  */
object TestExtension extends ExtensionId[TestExtensionImpl] with ExtensionIdProvider{
  override def createExtension(system: ExtendedActorSystem): TestExtensionImpl = new TestExtensionImpl(system)

  override def lookup(): ExtensionId[_ <: Extension] = TestExtension

}
class TestExtensionImpl(system: ExtendedActorSystem) extends Extension{

}
object AkkaExtension {
  def main(args: Array[String]): Unit = {

  }
}
