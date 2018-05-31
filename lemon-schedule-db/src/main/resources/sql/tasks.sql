CREATE TABLE `tasks` (
  `row_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '唯一ID，没有业务含义',
  `uid` varchar(64) NOT NULL COMMENT 'TASK.UID',
  `job_uid` varchar(64) NOT NULL COMMENT '作业UID',
  `schedule_uid` varchar(64) NOT NULL COMMENT '执行计划表的UID',
  `retry_id` int(11) NOT NULL COMMENT '当前的重试ID',
  `task_tracker_node` varchar(512) CHARACTER SET utf8mb4 NOT NULL COMMENT '当前任务执行所在节点',
  `state` varchar(16) CHARACTER SET utf8mb4 NOT NULL COMMENT '当前任务状态',
  `event_time` bigint(20) NOT NULL,
  `message` varchar(1024) CHARACTER SET utf8mb4 DEFAULT NULL,
  `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`row_id`),
  UNIQUE KEY `UK_TASK_INSTANCE` (`job_uid`,`schedule_uid`,`retry_id`,`state`),
  UNIQUE KEY `UK_TASK_UID` (`uid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='任务执行的历史表。任务每次执行的详情';
