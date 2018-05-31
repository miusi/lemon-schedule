CREATE TABLE `dependencies` (
  `row_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '唯一ID，没有业务含义',
  `uid` varchar(64) NOT NULL,
  `job_uid` varchar(64) NOT NULL COMMENT '作业UID',
  `depend_job_uid` varchar(64) NOT NULL COMMENT '依赖的作业UID',
  `time_offset` bigint(20) NOT NULL COMMENT '依赖作业的时间偏移量。当前任务时间+trigger_time_offset等于依赖的任务实例的时间',
  `time_offset_unit` varchar(16) CHARACTER SET utf8mb4 NOT NULL COMMENT '依赖作业的时间偏移量单位。当前任务时间+trigger_time_offset等于依赖的任务实例的时间',
  `time_offset_milli_sec` bigint(20) NOT NULL COMMENT '依赖作业的时间偏移量，单位是毫秒。根据time_offset和time_offset_unit计算而来',
  `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`row_id`),
  UNIQUE KEY `UK_DEPEND` (`job_uid`,`depend_job_uid`),
  UNIQUE KEY `UK_DEPEND_UID` (`uid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='作业依赖关系表';
