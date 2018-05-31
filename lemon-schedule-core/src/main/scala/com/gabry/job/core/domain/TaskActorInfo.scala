package com.gabry.job.core.domain

/**
  * Created by gabry on 2018/3/27 14:27
  */
/**
  * 给TaskActor提供信息
  * @param cluster 集群名称
  * @param group 组名称
  * @param claz job实例对应的Job类
  * @param classInfo job实例对应job类信息
  */
final case class TaskActorInfo(cluster:String,
                               group:String,
                               claz:Class[_],
                               classInfo:TaskClassInfo){
  override def toString: String = s"TaskActorInfo(cluster=$cluster,group=$group,claz=$claz,classInfo=$classInfo)"
}