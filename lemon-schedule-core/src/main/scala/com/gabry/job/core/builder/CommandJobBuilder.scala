package com.gabry.job.core.builder

/**
  * Created by gabry on 2018/4/28 13:01
  */
object CommandJobBuilder{
  def apply(): CommandJobBuilder = new CommandJobBuilder()
  val COMMAND_KEY = "shell.command"
  val WORK_DIR_KEY = "shell.workdir"
}
class CommandJobBuilder extends JobBuilder{
  /**
    * 设置命令行
    * @param command 待设置的命令行
    */
  def withCommand(command:String):this.type = {
    super.withMeta(CommandJobBuilder.COMMAND_KEY,command)
  }

  /**
    * 设置命令行执行的目录
    * @param workDir 命令行执行的目录
    */
  def withWorkDir(workDir:String):this.type ={
    super.withMeta(CommandJobBuilder.WORK_DIR_KEY,workDir)
  }

  /**
    * 设置命令行执行时的环境变量
    * @param envKey 环境变量的key值
    * @param envValue 环境变量的value
    */
  def withEnv(envKey:String,envValue:String):this.type =
    super.withMeta(envKey,envValue)

}
