package com.gabry.job.worker.tracker

import java.util.concurrent.{Callable, FutureTask}

import akka.actor.{Props, ReceiveTimeout}
import com.gabry.job.core.actor.SimpleActor
import com.gabry.job.core.command.{TaskActorCommand, TaskCommand}
import com.gabry.job.core.domain._
import com.gabry.job.core.event.{TaskActorEvent, TaskEvent, TaskRunnerEvent}
import com.gabry.job.core.task.Task
import com.gabry.job.utils.Utils

import scala.concurrent.Future
import scala.concurrent.duration._
import scala.util.{Failure, Success, Try}

/**
  * Created by gabry on 2018/3/23 17:32
  */
/**
  * 用actor封装task类,用来接收作业调度信息，控制并发数量
  * 一个TaskActor与一个Task具体实现一一对应；TaskActor调用Task对应的接口，进行初始化等操作，但是不负责具体的执行
  * @param taskActorInfo TaskActor参数信息
  */
class TaskActor(taskActorInfo:TaskActorInfo) extends SimpleActor{
  /**
    * 当前执行的Task数量，也就是并发数
    */
  private var executingTaskNum = 0
  /**
    * Task实例
    */
  private val task = taskActorInfo.claz.newInstance().asInstanceOf[Task]

  /**
    * 启动之前调用task.initialize()
    */
  override def preStart(): Unit = {
    super.preStart()
    task.initialize()
  }

  /**
    * 关闭之后调用task.destroy()
    */
  override def postStop(): Unit = {
    super.postStop()
    task.destroy()
  }

  /**
    * 处理收到的消息
    */
  override def userDefineEventReceive: Receive = {
    case runCmd @ TaskActorCommand.RunTask( jobContext,replyTo ) =>
      log.info(s"收到了runCmd命令 $runCmd")
      // 收到调度命令后，判断当前并发度是否满足条件，满足后创建TaskRunner执行具体的业务逻辑
      if( executingTaskNum <= taskActorInfo.classInfo.parallel ){

        executingTaskNum += 1

        val taskRunnerInfo = TaskRunnerInfo(taskActorInfo.cluster,taskActorInfo.group,task,taskActorInfo.classInfo)

        val taskRunner = context.actorOf(Props.create(classOf[TaskRunnerActor],taskRunnerInfo),jobContext.job.name+"-"+jobContext.schedule.triggerTime)
        context.watchWith(taskRunner,TaskRunnerEvent.Stopped(taskRunner))
        replyTo ! TaskEvent.Started(taskRunner,jobContext)

        taskRunner ! runCmd

      }else{
        // 如果超过了并发，则告诉发送方需要重新调度，发送方应该是schedulerNode
        // 重新调度时，并不一定还是当前节点
        // TODO: 2018年4月11日13:41:39 此处去掉并发的限制，
        replyTo ! TaskActorEvent.Overloaded(jobContext)
      }
    case evt @ TaskRunnerEvent.Stopped(taskRunner) =>
      val stopAt = System.currentTimeMillis()
      executingTaskNum -= 1
      log.warning(s"task runner $taskRunner stopped,alive time ${Utils.formatAliveTime(evt.at,stopAt)}")
  }
}

/**
  * 用actor封装task执行，控制重试、超时、取消等机制
  * @param taskRunnerInfo taskRunner信息
  */
class TaskRunnerActor(taskRunnerInfo:TaskRunnerInfo) extends SimpleActor{
  // 需要设置子actor的监督机制，确保失败后不会自动重启
  /**
    * 当前作业的执行命令
    */
  private var runCmd:TaskActorCommand.RunTask = _
  /**
    * 可中断的Task
    */
  private var interruptableTask:InterruptableTask = _
  private val checkDependencySleepTime = config.getDuration("worker.check-dependency-sleep-time").toMillis
  private var dependencyPassed = false
  private var sendWaiting = false
  // 如果没有达到指定次数则进行重试
  /**
    * 是否可以重新运行
    * @return 如果重试次数小于最大重试次数则可以重新运行
    */
  private def canReRun():Boolean = runCmd.jobContext.retryId < runCmd.jobContext.job.retryTimes

  override def userDefineEventReceive: Receive = {

    case evt @ TaskEvent.DependencyState(passed) =>
      dependencyPassed = passed
      if( evt.at - runCmd.jobContext.schedule.triggerTime > taskRunnerInfo.classInfo.defaultTimeOut.min(runCmd.jobContext.job.timeOut)*1000 ){
        self ! ReceiveTimeout
      }else{
        self ! runCmd
      }
    /**
      * 收到运行作业的命令
      * 首先检查作业的依赖是否成功，需要考虑检查作业的时限和次数，以及每次检查的时间间隔
      * 依赖检查通过后，就可以实际的运行作业了
      */
    case cmd:TaskActorCommand.RunTask =>
      runCmd = cmd

      if(dependencyPassed){
        interruptableTask = new InterruptableTask(taskRunnerInfo.task,runCmd.jobContext,taskRunnerInfo.classInfo.parallel)
        // 超时时间，取最小值，单位是秒
        val timeout = taskRunnerInfo.classInfo.defaultTimeOut.min(runCmd.jobContext.job.timeOut)

        // 设定当前作业的超时时间，超时后中断Task，重新调用
        context.setReceiveTimeout(timeout seconds)

        // 放到future执行，也就是放到另外一个单独线程异步执行
        // 执行结束需要发消息给self
        Future{
          runCmd.replyTo ! TaskEvent.Executing(runCmd.jobContext)
          interruptableTask.run()
        }.onComplete{
          case Success(runResult) =>
            log.debug(s"interruptableTask执行结果$runResult")
            interruptableTask.get() match {
              case Success(taskReturn) =>
                log.debug(s"作业执行成功 result = $taskReturn,get = ${interruptableTask.get()}")
                self ! TaskEvent.Succeed(runCmd.jobContext)
              case Failure(taskException)=>
                log.warning(s"作业执行出现异常 $taskException")
                self ! TaskEvent.Failed(taskException.getMessage,runCmd.jobContext)
            }
          case Failure(runException) =>
            log.warning(s"作业执行失败:${runException.getMessage}")
            self ! TaskEvent.Failed(runException.getMessage,runCmd.jobContext)
        }
      }else{
        if(!sendWaiting){
          runCmd.replyTo ! TaskEvent.Waiting(runCmd.jobContext)
          sendWaiting = true
        }
        // 此处休眠3秒，在发送运行作业的命令，防止死循环
        // 因为RunTask和DependencyState命令会循环触发
        Thread.sleep(checkDependencySleepTime)
        runCmd.replyTo ! TaskCommand.CheckDependency(runCmd.jobContext.job.uid,runCmd.jobContext.dataTime,self)
      }


    /**
      * 提前终止作业,直接退出，不再重试
      * 用户主动取消掉
      */
    // TODO: 2018年4月11日16:16:16 暂时取消该功能。具体由谁进行取消还没有定
//    case TaskActorCommand.CancelTask(replyTo) =>
//      context.setReceiveTimeout(Duration.Undefined)
//      val interrupt = interruptableTask.interrupt()
//      log.warning(s" ${runCmd.jobContext} 作业被终止:$interrupt")
//      val cancelEvent = TaskEvent.Cancelled(replyTo,runCmd.jobContext)
//      replyTo ! cancelEvent
//      runCmd.replyTo ! cancelEvent
//      context.stop(self)

    /**
      * 作业执行超时，杀掉作业并重新运行，如果重试失败则退出
      */
    case timeout:ReceiveTimeout =>
      val interrupt = interruptableTask.interrupt()
      log.info(s"作业执行超时 $timeout，中断作业 $interrupt")
      runCmd.replyTo ! TaskEvent.Timeout(runCmd.jobContext)
      context.setReceiveTimeout(Duration.Undefined)
      if(canReRun()){
        self ! runCmd.copy(jobContext = runCmd.jobContext.copy(retryId = runCmd.jobContext.retryId + 1 ))
      }else{
        // 如果reRun失败则退出此次调用
        runCmd.replyTo ! TaskEvent.TimeOutStopped(runCmd.jobContext)
        context.stop(self)
      }

    /**
      * 作业执行成功
      */
    case evt @ TaskEvent.Succeed(jobContext) =>
      // 作业执行成功后，将成功消息发送给汇报者
      // 注意，此处不能是原来的jobContext，因为task执行时可能会修改jobContext
      runCmd.replyTo ! evt
      log.info(s"$jobContext 执行成功，现在退出")
      context.stop(self)

    /**
      * 作业执行失败
      */
    case evt :TaskEvent.Failed =>
      log.info(s"$evt 执行失败")
      context.setReceiveTimeout(Duration.Undefined)
      runCmd.replyTo ! evt
      if(canReRun()){
        self ! runCmd.copy(jobContext = runCmd.jobContext.copy(retryId = runCmd.jobContext.retryId + 1 ))
      }else{
        // 如果reRun失败则退出此次调用
        runCmd.replyTo ! TaskEvent.MaxRetryReached(runCmd.jobContext)
        context.stop(self)
      }
  }
}

/**
  * 用Callable封装task的具体执行，这样就可以
  * @param task 封装的task
  * @param jobContext 作业执行上下文
  * @param parallel 作业并发度
  */
class InterruptableTask(task:Task, jobContext: JobContext, parallel:Int){

  /**
    * 封装Task，使其可中断
    * @param task 封装的task
    * @param jobContext 作业执行上下文
    * @param taskIndex 作业需要
    * @param parallel 作业并发度
    */
  private[this] class InnerTask(task:Task, jobContext: JobContext, taskIndex:Int, parallel:Int) extends Callable[Long]{
    override def call(): Long = {
      task.run(jobContext,taskIndex,parallel)
      Thread.currentThread().getId
    }
  }

  /**
    * FutureTask封装Task使其可以中断
    */
  private val futureTask = new FutureTask[Long](new InnerTask(task,jobContext,jobContext.retryId,parallel))


  /**
    * 运行Task
    */
  def run():Unit = futureTask.run()

  /**
    * 中断Task
    */
  def interrupt():Boolean = futureTask.cancel(true)

  /**
    * 获取执行结果
    * @return 执行结果的Try封装
    */
  def get():Try[Long] = Try(futureTask.get())
}