package com.gabry.job.utils

import java.io.File
import java.net.{URL, URLClassLoader}

import scala.reflect.ClassTag
import scala.util.{Failure, Success, Try}

/**
  * Created by gabry on 2018/3/26 15:55
  */
/**
  * Task类加载器
  * @param jarFilePath 对应jar包的路径
  */
class TaskClassLoader(jarFilePath:String) {
  private var urlClassLoader:URLClassLoader = _

  /**
    * 初始化类加载器
    */
  def init():Unit = {
    if(urlClassLoader==null){
      val canonicalPath = new File(jarFilePath)
      val path = new File(s"file://${canonicalPath.getCanonicalPath}")
      val urls = if(path.isDirectory) path.listFiles().map(file=>new URL(file.getPath)) else Array(new URL(path.getPath))
      urlClassLoader = new URLClassLoader(urls)
    }
  }
  def getUrls:Array[URL] = urlClassLoader.getURLs
  /**
    * 加载指定的类
    * @param className 类名称
    * @return 加载后的类
    */
  def load(className:String): Try[Class[_]] = Try(urlClassLoader.loadClass(className))

  /**
    * 加载指定的类，并创建对应的实例
    * @param className 类名称
    * @param rt 类型的运行时信息
    * @tparam T 类名称对应的类型
    * @return 加载后的类实例
    */
  def loadInstance[T:ClassTag](className:String)(implicit rt:ClassTag[T]): Try[T] = load(className) match {
    case Success(claz) if rt.runtimeClass.isAssignableFrom(claz) =>
      Success(claz.newInstance().asInstanceOf[T])
    case Success(claz) =>
      Failure(new ClassCastException(s"${claz.getClass} cannot be cast to $className"))
    case Failure(reason) =>
      Failure(reason)
  }

  /**
    * 卸载当前jar包
    */
  def destroy():Unit =
    if(urlClassLoader != null){
      urlClassLoader.close()
    }

}
