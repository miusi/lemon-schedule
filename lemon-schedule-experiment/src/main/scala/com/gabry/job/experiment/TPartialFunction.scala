package com.gabry.job.experiment

/**
  * Created by gabry on 2018/5/24 14:30
  */
object TPartialFunction {
  trait No{
    def no:Option[Int]
  }
  case class EmpNo(no:Option[Int]) extends No
  case class ProNo(no:Option[Int]) extends No
  val getEmpNo:PartialFunction[No,Unit] = {
    case EmpNo(Some(empNo)) =>
      println(s"empNo: $empNo")
  }
  val getProNo:PartialFunction[No,Unit] = {
    case ProNo(Some(proNo)) =>
      println(s"proNo: $proNo")
  }
  def getNo(no:No):Unit = {
    val get = getEmpNo orElse getProNo
    get(no)
  }
  def main(args: Array[String]): Unit = {
    getNo(EmpNo(Some(1)))
    getNo(ProNo(Some(2)))
  }
}
