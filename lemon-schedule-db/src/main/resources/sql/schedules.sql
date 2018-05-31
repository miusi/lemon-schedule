CREATE TABLE `schedules` (
  `row_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '唯一ID，没有业务含义',
  `uid` varchar(64) NOT NULL COMMENT '作业UID',
  `job_uid` varchar(64) NOT NULL COMMENT '作业UID',
  `priority` int(11) NOT NULL COMMENT '优先级。优先级可动态调整，此处的值跟Job中可能不同 。调度时根据改值进行排序调度',
  `retry_times` int(11) NOT NULL COMMENT '重试次数，与job中的值也可能不同',
  `dispatched` tinyint(1) NOT NULL DEFAULT '0' COMMENT '当前计划是否被调度。用来判断是否需要被调度',
  `trigger_time` bigint(20) NOT NULL COMMENT '此次任务的触发时间。到达该时间立即执行作业',
  `schedule_node` varchar(64) CHARACTER SET utf8mb4 NOT NULL,
  `schedule_time` bigint(20) NOT NULL,
  `succeed` tinyint(1) NOT NULL DEFAULT '0' COMMENT '当前任务是否执行成功。即任务的最终状态是否为成功，成功为true，否则为false',
  `data_time` bigint(20) NOT NULL COMMENT '此次任务处理数据的时间',
  `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '记录的创建时间，与业务无关',
  PRIMARY KEY (`row_id`),
  UNIQUE KEY `UK_SCHEDULE_INST` (`job_uid`,`trigger_time`),
  UNIQUE KEY `UK_SCHEDULE_UID` (`uid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='任务执行计划表，Scheduler严格根据此表调度作业';
