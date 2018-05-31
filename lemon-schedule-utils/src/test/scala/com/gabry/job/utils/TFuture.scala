package com.gabry.job.utils

import org.scalatest.FunSuite

import scala.concurrent.{ExecutionContextExecutor, Future}
import scala.util.{Failure, Success}

/**
  * Created by gabry on 2018/4/19 14:18
  */
case class TaggedException(index:Int,exception: Throwable)extends Throwable{
  override def toString: String = s"TaggedException(index=$index,exception=$exception)"
}

class TFuture extends FunSuite {
  implicit lazy val executionContext: ExecutionContextExecutor = scala.concurrent.ExecutionContext.global
  def tagFailedWithIndex[T](index:Int,f:Future[T]):Future[T] = {
    f.recoverWith{
      case exception =>
        Future.failed(TaggedException(index,exception))
    }
  }
  test("two future with first error"){
    val first = Future{
      Thread.sleep(1*1000)
      throw new Exception("first future has error")
    }
    val second = Future{
      Thread.sleep(2*1000)
      2
    }
    val result = for{
      f<-first
      s<-second
    }yield (f,s)
    result.onComplete{
      case Success(s)=>
        println(s"s=$s")
      case Failure(f)=>
        println(s"f=$f")
        println(s"first=${first.isCompleted},second=${second.isCompleted}")
    }

    Thread.sleep(3*1000)
  }
  test("two future with second error"){
   val first = Future{
     Thread.sleep(1*1000)
     1
    }
    val second = Future{
      Thread.sleep(2*1000)
      throw new Exception("second future has error")
    }
    val result = for{
      f<-first
      s<-second
    }yield (f,s)
    result.onComplete{
      case Success(s)=>
        println(s"s=$s")
      case Failure(f)=>
        println(s"f=$f")
        println(s"first=${first.isCompleted},second=${second.isCompleted}")
    }

    Thread.sleep(3*1000)
  }
  test("two future with both error"){
    val first = Future{
      Thread.sleep(1*1000)
      throw new Exception("first future has error")
    }
    val second = Future{
      Thread.sleep(2*1000)
      throw new Exception("second future has error")
    }
    val result = for{
      f<-first
      s<-second
    }yield (f,s)
    result.onComplete{
      case Success(s)=>
        println(s"s=$s")
      case Failure(f)=>
        println(s"f=$f")
        println(s"first=${first.isCompleted},second=${second.isCompleted}")
    }
    Thread.sleep(3*1000)
  }
  test("two future with no error"){
    val first = Future{
      Thread.sleep(1*1000)
      1
    }
    val second = Future{
      Thread.sleep(2*1000)
      2
    }
    val result = for{
      f<-first
      s<-second
    }yield (f,s)
    result.onComplete{
      case Success(s)=>
        println(s"s=$s")
        println(s"first=${first.isCompleted},second=${second.isCompleted}")
      case Failure(f)=>
        println(s"f=$f")
    }

    Thread.sleep(3*1000)

  }
  test("two future with TaggedException"){
    val first = Future{
      Thread.sleep(1*1000)
      throw new Exception("first future has error")
    }
    val second = Future{
      Thread.sleep(2*1000)
      throw new Exception("second future has error")
    }
    val result = for{
      f<-tagFailedWithIndex(1,first)
      s<-tagFailedWithIndex(2,second)
    }yield (f,s)
    result.onComplete{
      case Success(s)=>
        println(s"s=$s")
      case Failure(f)=>
        println(s"f=$f")
        println(s"first=${first.isCompleted},second=${second.isCompleted}")

    }

    Thread.sleep(3*1000)

  }
}
