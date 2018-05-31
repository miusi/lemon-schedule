package com.gabry.job.test

import com.gabry.job.core.builder.CommandJobBuilder
import com.gabry.job.core.domain.JobContext
import com.gabry.job.core.task.impl.CommandTask
import org.scalatest.FunSuite

/**
  * Created by gabry on 2018/4/28 13:37
  */
class TTask extends FunSuite {
  test("TaskExample commandTask"){
    val job = CommandJobBuilder()
        .withEnv("testEnv","envValue")
      .withCommand("ls -l")
      .build()
    val jobContext = JobContext(job,null,0)
    val task = new CommandTask
    task.run(jobContext,0,0)
  }
}
