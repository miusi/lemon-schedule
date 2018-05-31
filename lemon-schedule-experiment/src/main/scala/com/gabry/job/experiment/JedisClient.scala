package com.gabry.job.experiment

import redis.clients.jedis.{JedisPool, JedisPoolConfig}

/**
  * Created by gabry on 2018/5/3 13:57
  */
object JedisClient {
  def main(args: Array[String]): Unit = {
    val jedisPool = new JedisPool(new JedisPoolConfig,"localhost")
    val jedis = jedisPool.getResource
    jedis.lpush("key","keyValue")
    jedis.pipelined().sync()
    jedis.close()
    jedisPool.close()
  }
}
