package com.gabry.job.db.slicks
import slick.jdbc.MySQLProfile.api._
/**
  * Created by gabry on 2018/5/7 10:18
  * 为所有的DataAccess类提供构造函数的形式
  * 不提供对外的接口，接口统一由DataAccess暴露
  */
abstract class SlickDataAccess(database:Database) {

}
