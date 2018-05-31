package com.gabry.job.core.domain

import com.gabry.job.core.task.Task

/**
  * Created by gabry on 2018/4/3 16:44
  * 给TaskRunner提供信息
  * @param cluster 集群名称
  * @param group 组名称
  * @param task Task实例
  * @param classInfo Task类信息
  */
final case class TaskRunnerInfo (cluster:String, group:String, task:Task, classInfo:TaskClassInfo){
  override def toString: String = s"TaskRunnerInfo(cluster=$cluster,group=$group,task=$task,classInfo=$classInfo)"
}