package com.gabry.job.db.factory

import com.gabry.job.db.access._
import com.gabry.job.db.slicks.SlickDataAccessFactory
import com.gabry.job.utils.ExternalClassHelper._
import com.typesafe.config.{Config, ConfigFactory}

import scala.util.{Failure, Success, Try}
/**
  * Created by gabry on 2018/5/3 14:06
  */
object DatabaseFactory {

  def getDataAccessFactory:Try[DataAccessFactory] =
    getDataAccessFactory(ConfigFactory.load())

  def getDataAccessFactory(config:Config):Try[DataAccessFactory] =
    getDataAccessFactory(config.getStringOr("db.type","slick"),config)

  def getDataAccessFactory(databaseType:String, config:Config):Try[DataAccessFactory] = {
    databaseType.toLowerCase match {
      case "slick" =>
        Success(new SlickDataAccessFactory(config.getConfig(s"db.$databaseType")))
      case "quill" =>
        Failure(new UnsupportedOperationException("quill unsupported database driver! json field not supported ! "))
      case unknownDatabaseType =>
        Failure(new IllegalArgumentException(s"unknown database type $unknownDatabaseType,supported database type is [slick]"))
    }
  }

}
