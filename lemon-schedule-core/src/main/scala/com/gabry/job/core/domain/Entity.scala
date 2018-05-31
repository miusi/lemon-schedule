package com.gabry.job.core.domain

/**
  * Created by gabry on 2018/3/28 19:53
  * 实体trait
  * 限制必须拥有id字段且可序列化
  */
trait Entity extends Serializable{
  def uid:UID
}
