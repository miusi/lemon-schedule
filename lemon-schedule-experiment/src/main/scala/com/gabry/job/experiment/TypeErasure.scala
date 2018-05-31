package com.gabry.job.experiment

/**
  * Created by gabry on 2018/5/14 16:18
  */
trait Messages[T]{
  def message:T
}
final case class WrapMessage[T](source:Messages[T])
object Messages{
  final case class MyMessage1[T](message:T) extends Messages[T]
  final case class MyMessage2[T](message:T) extends Messages[T]
}


object TypeErasure {
  def printlnMessage[T](event:Messages[T]):Unit = {
    event match {
      case evt @ Messages.MyMessage1(message:String)=>
        println(s"event=$evt,type=${evt.getClass}")
        println(WrapMessage(evt))
      case any =>
        println(WrapMessage(any))

        println(s"event=$event")
    }
  }
  def main(args: Array[String]): Unit = {
    printlnMessage(Messages.MyMessage1("StringMessage"))
    printlnMessage(Messages.MyMessage1(111))
  }
}
