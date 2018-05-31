package com.gabry.job.core.builder

/**
  * Created by gabry on 2018/4/28 13:10
  */
object HiveJobBuilder{
  def apply(): HiveJobBuilder = new HiveJobBuilder()
}
class HiveJobBuilder extends JobBuilder {

}
