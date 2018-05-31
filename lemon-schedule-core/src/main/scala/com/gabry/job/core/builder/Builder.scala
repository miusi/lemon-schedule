package com.gabry.job.core.builder

/**
  * Created by gabry on 2018/4/24 9:46
  */
trait Builder[T] {
  /**
    * 创建出对应的对象
    * @return 创建后的对象
    */
  def build():T
}
