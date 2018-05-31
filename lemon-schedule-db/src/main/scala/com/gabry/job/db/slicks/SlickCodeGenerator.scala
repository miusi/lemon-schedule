package com.gabry.job.db.slicks

/**
  * Created by gabry on 2018/3/29 15:31
  */
object SlickCodeGenerator {
  def main(args: Array[String]): Unit = {
    slick.codegen.SourceCodeGenerator.main(Array("slick.jdbc.MySQLProfile"
      ,"com.mysql.cj.jdbc.Driver"
    ,"jdbc:mysql://dn18:3306/akka_job?useUnicode=true&characterEncoding=UTF-8&useSSL=false&nullNamePatternMatchesAll=true"
    ,"D:/MyCode/lemon-schedule/lemon-schedule-db/src/main/scala"
    ,"com.gabry.job.db.slicks.schema","root","mysql")
    )
  }
}
