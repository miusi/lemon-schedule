package com.gabry.job.db.slicks.schema
// AUTO-GENERATED Slick data model
/** Stand-alone Slick data model for immediate use */
object Tables extends {
  val profile = slick.jdbc.MySQLProfile
} with Tables

/** Slick data model trait for extension, choice of backend or usage in the cake pattern. (Make sure to initialize this late.) */
trait Tables {
  val profile: slick.jdbc.JdbcProfile
  import profile.api._
  // NOTE: GetResult mappers for plain SQL are only generated for tables where Slick knows how to map the types of all columns.
  import slick.jdbc.{GetResult => GR}

  /** DDL for all tables. Call .create to execute. */
  lazy val schema: profile.SchemaDescription = Dependencies.schema ++ Jobs.schema ++ Schedules.schema ++ Tasks.schema
  @deprecated("Use .schema instead of .ddl", "3.0")
  def ddl = schema

  /** Entity class storing rows of table Dependencies
   *  @param rowId Database column row_id SqlType(BIGINT), AutoInc, PrimaryKey
   *  @param uid Database column uid SqlType(VARCHAR), Length(64,true)
   *  @param jobUid Database column job_uid SqlType(VARCHAR), Length(64,true)
   *  @param dependJobUid Database column depend_job_uid SqlType(VARCHAR), Length(64,true)
   *  @param timeOffset Database column time_offset SqlType(BIGINT)
   *  @param timeOffsetUnit Database column time_offset_unit SqlType(VARCHAR), Length(16,true)
   *  @param timeOffsetMilliSec Database column time_offset_milli_sec SqlType(BIGINT)
   *  @param updateTime Database column update_time SqlType(TIMESTAMP) */
  case class DependenciesRow(rowId: Long, uid: String, jobUid: String, dependJobUid: String, timeOffset: Long, timeOffsetUnit: String, timeOffsetMilliSec: Long, updateTime: java.sql.Timestamp)
  /** GetResult implicit for fetching DependenciesRow objects using plain SQL queries */
  implicit def GetResultDependenciesRow(implicit e0: GR[Long], e1: GR[String], e2: GR[java.sql.Timestamp]): GR[DependenciesRow] = GR{
    prs => import prs._
    DependenciesRow.tupled((<<[Long], <<[String], <<[String], <<[String], <<[Long], <<[String], <<[Long], <<[java.sql.Timestamp]))
  }
  /** Table description of table dependencies. Objects of this class serve as prototypes for rows in queries. */
  class Dependencies(_tableTag: Tag) extends profile.api.Table[DependenciesRow](_tableTag, Some("akka_job"), "dependencies") {
    def * = (rowId, uid, jobUid, dependJobUid, timeOffset, timeOffsetUnit, timeOffsetMilliSec, updateTime) <> (DependenciesRow.tupled, DependenciesRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (Rep.Some(rowId), Rep.Some(uid), Rep.Some(jobUid), Rep.Some(dependJobUid), Rep.Some(timeOffset), Rep.Some(timeOffsetUnit), Rep.Some(timeOffsetMilliSec), Rep.Some(updateTime)).shaped.<>({r=>import r._; _1.map(_=> DependenciesRow.tupled((_1.get, _2.get, _3.get, _4.get, _5.get, _6.get, _7.get, _8.get)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column row_id SqlType(BIGINT), AutoInc, PrimaryKey */
    val rowId: Rep[Long] = column[Long]("row_id", O.AutoInc, O.PrimaryKey)
    /** Database column uid SqlType(VARCHAR), Length(64,true) */
    val uid: Rep[String] = column[String]("uid", O.Length(64,varying=true))
    /** Database column job_uid SqlType(VARCHAR), Length(64,true) */
    val jobUid: Rep[String] = column[String]("job_uid", O.Length(64,varying=true))
    /** Database column depend_job_uid SqlType(VARCHAR), Length(64,true) */
    val dependJobUid: Rep[String] = column[String]("depend_job_uid", O.Length(64,varying=true))
    /** Database column time_offset SqlType(BIGINT) */
    val timeOffset: Rep[Long] = column[Long]("time_offset")
    /** Database column time_offset_unit SqlType(VARCHAR), Length(16,true) */
    val timeOffsetUnit: Rep[String] = column[String]("time_offset_unit", O.Length(16,varying=true))
    /** Database column time_offset_milli_sec SqlType(BIGINT) */
    val timeOffsetMilliSec: Rep[Long] = column[Long]("time_offset_milli_sec")
    /** Database column update_time SqlType(TIMESTAMP) */
    val updateTime: Rep[java.sql.Timestamp] = column[java.sql.Timestamp]("update_time")

    /** Uniqueness Index over (jobUid,dependJobUid) (database name UK_DEPEND) */
    val index1 = index("UK_DEPEND", (jobUid, dependJobUid), unique=true)
    /** Uniqueness Index over (uid) (database name UK_DEPEND_UID) */
    val index2 = index("UK_DEPEND_UID", uid, unique=true)
  }
  /** Collection-like TableQuery object for table Dependencies */
  lazy val Dependencies = new TableQuery(tag => new Dependencies(tag))

  /** Entity class storing rows of table Jobs
   *  @param rowId Database column row_id SqlType(BIGINT), AutoInc, PrimaryKey
   *  @param uid Database column uid SqlType(VARCHAR), Length(64,true)
   *  @param name Database column name SqlType(VARCHAR), Length(32,true)
   *  @param className Database column class_name SqlType(VARCHAR), Length(64,true)
   *  @param metaData Database column meta_data SqlType(JSON), Length(1073741824,true)
   *  @param dataTimeOffset Database column data_time_offset SqlType(BIGINT)
   *  @param dataTimeOffsetUnit Database column data_time_offset_unit SqlType(VARCHAR), Length(16,true)
   *  @param startTime Database column start_time SqlType(BIGINT)
   *  @param cron Database column cron SqlType(VARCHAR), Length(32,true)
   *  @param priority Database column priority SqlType(INT)
   *  @param parallel Database column parallel SqlType(INT)
   *  @param retryTimes Database column retry_times SqlType(INT)
   *  @param workerNodes Database column worker_nodes SqlType(VARCHAR), Length(1024,true), Default(None)
   *  @param clusterName Database column cluster_name SqlType(VARCHAR), Length(16,true)
   *  @param groupName Database column group_name SqlType(VARCHAR), Length(32,true)
   *  @param timeout Database column timeout SqlType(INT)
   *  @param replaceIfExist Database column replace_if_exist SqlType(BIT)
   *  @param lastGenerateTriggerTime Database column last_generate_trigger_time SqlType(BIGINT), Default(None)
   *  @param schedulerNode Database column scheduler_node SqlType(VARCHAR), Length(64,true), Default(None)
   *  @param scheduleFrequency Database column schedule_frequency SqlType(BIGINT), Default(None)
   *  @param lastScheduleTime Database column last_schedule_time SqlType(BIGINT), Default(None)
   *  @param updateTime Database column update_time SqlType(TIMESTAMP) */
  case class JobsRow(rowId: Long, uid: String, name: String, className: String, metaData: String, dataTimeOffset: Long, dataTimeOffsetUnit: String, startTime: Long, cron: String, priority: Int, parallel: Int, retryTimes: Int, workerNodes: Option[String] = None, clusterName: String, groupName: String, timeout: Int, replaceIfExist: Boolean, lastGenerateTriggerTime: Option[Long] = None, schedulerNode: Option[String] = None, scheduleFrequency: Option[Long] = None, lastScheduleTime: Option[Long] = None, updateTime: java.sql.Timestamp)
  /** GetResult implicit for fetching JobsRow objects using plain SQL queries */
  implicit def GetResultJobsRow(implicit e0: GR[Long], e1: GR[String], e2: GR[Int], e3: GR[Option[String]], e4: GR[Boolean], e5: GR[Option[Long]], e6: GR[java.sql.Timestamp]): GR[JobsRow] = GR{
    prs => import prs._
    JobsRow.tupled((<<[Long], <<[String], <<[String], <<[String], <<[String], <<[Long], <<[String], <<[Long], <<[String], <<[Int], <<[Int], <<[Int], <<?[String], <<[String], <<[String], <<[Int], <<[Boolean], <<?[Long], <<?[String], <<?[Long], <<?[Long], <<[java.sql.Timestamp]))
  }
  /** Table description of table jobs. Objects of this class serve as prototypes for rows in queries. */
  class Jobs(_tableTag: Tag) extends profile.api.Table[JobsRow](_tableTag, Some("akka_job"), "jobs") {
    def * = (rowId, uid, name, className, metaData, dataTimeOffset, dataTimeOffsetUnit, startTime, cron, priority, parallel, retryTimes, workerNodes, clusterName, groupName, timeout, replaceIfExist, lastGenerateTriggerTime, schedulerNode, scheduleFrequency, lastScheduleTime, updateTime) <> (JobsRow.tupled, JobsRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (Rep.Some(rowId), Rep.Some(uid), Rep.Some(name), Rep.Some(className), Rep.Some(metaData), Rep.Some(dataTimeOffset), Rep.Some(dataTimeOffsetUnit), Rep.Some(startTime), Rep.Some(cron), Rep.Some(priority), Rep.Some(parallel), Rep.Some(retryTimes), workerNodes, Rep.Some(clusterName), Rep.Some(groupName), Rep.Some(timeout), Rep.Some(replaceIfExist), lastGenerateTriggerTime, schedulerNode, scheduleFrequency, lastScheduleTime, Rep.Some(updateTime)).shaped.<>({r=>import r._; _1.map(_=> JobsRow.tupled((_1.get, _2.get, _3.get, _4.get, _5.get, _6.get, _7.get, _8.get, _9.get, _10.get, _11.get, _12.get, _13, _14.get, _15.get, _16.get, _17.get, _18, _19, _20, _21, _22.get)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column row_id SqlType(BIGINT), AutoInc, PrimaryKey */
    val rowId: Rep[Long] = column[Long]("row_id", O.AutoInc, O.PrimaryKey)
    /** Database column uid SqlType(VARCHAR), Length(64,true) */
    val uid: Rep[String] = column[String]("uid", O.Length(64,varying=true))
    /** Database column name SqlType(VARCHAR), Length(32,true) */
    val name: Rep[String] = column[String]("name", O.Length(32,varying=true))
    /** Database column class_name SqlType(VARCHAR), Length(64,true) */
    val className: Rep[String] = column[String]("class_name", O.Length(64,varying=true))
    /** Database column meta_data SqlType(JSON), Length(1073741824,true) */
    val metaData: Rep[String] = column[String]("meta_data", O.Length(1073741824,varying=true))
    /** Database column data_time_offset SqlType(BIGINT) */
    val dataTimeOffset: Rep[Long] = column[Long]("data_time_offset")
    /** Database column data_time_offset_unit SqlType(VARCHAR), Length(16,true) */
    val dataTimeOffsetUnit: Rep[String] = column[String]("data_time_offset_unit", O.Length(16,varying=true))
    /** Database column start_time SqlType(BIGINT) */
    val startTime: Rep[Long] = column[Long]("start_time")
    /** Database column cron SqlType(VARCHAR), Length(32,true) */
    val cron: Rep[String] = column[String]("cron", O.Length(32,varying=true))
    /** Database column priority SqlType(INT) */
    val priority: Rep[Int] = column[Int]("priority")
    /** Database column parallel SqlType(INT) */
    val parallel: Rep[Int] = column[Int]("parallel")
    /** Database column retry_times SqlType(INT) */
    val retryTimes: Rep[Int] = column[Int]("retry_times")
    /** Database column worker_nodes SqlType(VARCHAR), Length(1024,true), Default(None) */
    val workerNodes: Rep[Option[String]] = column[Option[String]]("worker_nodes", O.Length(1024,varying=true), O.Default(None))
    /** Database column cluster_name SqlType(VARCHAR), Length(16,true) */
    val clusterName: Rep[String] = column[String]("cluster_name", O.Length(16,varying=true))
    /** Database column group_name SqlType(VARCHAR), Length(32,true) */
    val groupName: Rep[String] = column[String]("group_name", O.Length(32,varying=true))
    /** Database column timeout SqlType(INT) */
    val timeout: Rep[Int] = column[Int]("timeout")
    /** Database column replace_if_exist SqlType(BIT) */
    val replaceIfExist: Rep[Boolean] = column[Boolean]("replace_if_exist")
    /** Database column last_generate_trigger_time SqlType(BIGINT), Default(None) */
    val lastGenerateTriggerTime: Rep[Option[Long]] = column[Option[Long]]("last_generate_trigger_time", O.Default(None))
    /** Database column scheduler_node SqlType(VARCHAR), Length(64,true), Default(None) */
    val schedulerNode: Rep[Option[String]] = column[Option[String]]("scheduler_node", O.Length(64,varying=true), O.Default(None))
    /** Database column schedule_frequency SqlType(BIGINT), Default(None) */
    val scheduleFrequency: Rep[Option[Long]] = column[Option[Long]]("schedule_frequency", O.Default(None))
    /** Database column last_schedule_time SqlType(BIGINT), Default(None) */
    val lastScheduleTime: Rep[Option[Long]] = column[Option[Long]]("last_schedule_time", O.Default(None))
    /** Database column update_time SqlType(TIMESTAMP) */
    val updateTime: Rep[java.sql.Timestamp] = column[java.sql.Timestamp]("update_time")

    /** Uniqueness Index over (uid) (database name UK_JOB_UID) */
    val index1 = index("UK_JOB_UID", uid, unique=true)
    /** Uniqueness Index over (name) (database name UK_NAME) */
    val index2 = index("UK_NAME", name, unique=true)
  }
  /** Collection-like TableQuery object for table Jobs */
  lazy val Jobs = new TableQuery(tag => new Jobs(tag))

  /** Entity class storing rows of table Schedules
   *  @param rowId Database column row_id SqlType(BIGINT), AutoInc, PrimaryKey
   *  @param uid Database column uid SqlType(VARCHAR), Length(64,true)
   *  @param jobUid Database column job_uid SqlType(VARCHAR), Length(64,true)
   *  @param priority Database column priority SqlType(INT)
   *  @param retryTimes Database column retry_times SqlType(INT)
   *  @param dispatched Database column dispatched SqlType(BIT), Default(false)
   *  @param triggerTime Database column trigger_time SqlType(BIGINT)
   *  @param scheduleNode Database column schedule_node SqlType(VARCHAR), Length(64,true)
   *  @param scheduleTime Database column schedule_time SqlType(BIGINT)
   *  @param succeed Database column succeed SqlType(BIT), Default(false)
   *  @param dataTime Database column data_time SqlType(BIGINT)
   *  @param updateTime Database column update_time SqlType(TIMESTAMP) */
  case class SchedulesRow(rowId: Long, uid: String, jobUid: String, priority: Int, retryTimes: Int, dispatched: Boolean = false, triggerTime: Long, scheduleNode: String, scheduleTime: Long, succeed: Boolean = false, dataTime: Long, updateTime: java.sql.Timestamp)
  /** GetResult implicit for fetching SchedulesRow objects using plain SQL queries */
  implicit def GetResultSchedulesRow(implicit e0: GR[Long], e1: GR[String], e2: GR[Int], e3: GR[Boolean], e4: GR[java.sql.Timestamp]): GR[SchedulesRow] = GR{
    prs => import prs._
    SchedulesRow.tupled((<<[Long], <<[String], <<[String], <<[Int], <<[Int], <<[Boolean], <<[Long], <<[String], <<[Long], <<[Boolean], <<[Long], <<[java.sql.Timestamp]))
  }
  /** Table description of table schedules. Objects of this class serve as prototypes for rows in queries. */
  class Schedules(_tableTag: Tag) extends profile.api.Table[SchedulesRow](_tableTag, Some("akka_job"), "schedules") {
    def * = (rowId, uid, jobUid, priority, retryTimes, dispatched, triggerTime, scheduleNode, scheduleTime, succeed, dataTime, updateTime) <> (SchedulesRow.tupled, SchedulesRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (Rep.Some(rowId), Rep.Some(uid), Rep.Some(jobUid), Rep.Some(priority), Rep.Some(retryTimes), Rep.Some(dispatched), Rep.Some(triggerTime), Rep.Some(scheduleNode), Rep.Some(scheduleTime), Rep.Some(succeed), Rep.Some(dataTime), Rep.Some(updateTime)).shaped.<>({r=>import r._; _1.map(_=> SchedulesRow.tupled((_1.get, _2.get, _3.get, _4.get, _5.get, _6.get, _7.get, _8.get, _9.get, _10.get, _11.get, _12.get)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column row_id SqlType(BIGINT), AutoInc, PrimaryKey */
    val rowId: Rep[Long] = column[Long]("row_id", O.AutoInc, O.PrimaryKey)
    /** Database column uid SqlType(VARCHAR), Length(64,true) */
    val uid: Rep[String] = column[String]("uid", O.Length(64,varying=true))
    /** Database column job_uid SqlType(VARCHAR), Length(64,true) */
    val jobUid: Rep[String] = column[String]("job_uid", O.Length(64,varying=true))
    /** Database column priority SqlType(INT) */
    val priority: Rep[Int] = column[Int]("priority")
    /** Database column retry_times SqlType(INT) */
    val retryTimes: Rep[Int] = column[Int]("retry_times")
    /** Database column dispatched SqlType(BIT), Default(false) */
    val dispatched: Rep[Boolean] = column[Boolean]("dispatched", O.Default(false))
    /** Database column trigger_time SqlType(BIGINT) */
    val triggerTime: Rep[Long] = column[Long]("trigger_time")
    /** Database column schedule_node SqlType(VARCHAR), Length(64,true) */
    val scheduleNode: Rep[String] = column[String]("schedule_node", O.Length(64,varying=true))
    /** Database column schedule_time SqlType(BIGINT) */
    val scheduleTime: Rep[Long] = column[Long]("schedule_time")
    /** Database column succeed SqlType(BIT), Default(false) */
    val succeed: Rep[Boolean] = column[Boolean]("succeed", O.Default(false))
    /** Database column data_time SqlType(BIGINT) */
    val dataTime: Rep[Long] = column[Long]("data_time")
    /** Database column update_time SqlType(TIMESTAMP) */
    val updateTime: Rep[java.sql.Timestamp] = column[java.sql.Timestamp]("update_time")

    /** Uniqueness Index over (jobUid,triggerTime) (database name UK_SCHEDULE_INST) */
    val index1 = index("UK_SCHEDULE_INST", (jobUid, triggerTime), unique=true)
    /** Uniqueness Index over (uid) (database name UK_SCHEDULE_UID) */
    val index2 = index("UK_SCHEDULE_UID", uid, unique=true)
  }
  /** Collection-like TableQuery object for table Schedules */
  lazy val Schedules = new TableQuery(tag => new Schedules(tag))

  /** Entity class storing rows of table Tasks
   *  @param rowId Database column row_id SqlType(BIGINT), AutoInc, PrimaryKey
   *  @param uid Database column uid SqlType(VARCHAR), Length(64,true)
   *  @param jobUid Database column job_uid SqlType(VARCHAR), Length(64,true)
   *  @param scheduleUid Database column schedule_uid SqlType(VARCHAR), Length(64,true)
   *  @param retryId Database column retry_id SqlType(INT)
   *  @param taskTrackerNode Database column task_tracker_node SqlType(VARCHAR), Length(512,true)
   *  @param state Database column state SqlType(VARCHAR), Length(16,true)
   *  @param eventTime Database column event_time SqlType(BIGINT)
   *  @param message Database column message SqlType(VARCHAR), Length(1024,true), Default(None)
   *  @param updateTime Database column update_time SqlType(TIMESTAMP) */
  case class TasksRow(rowId: Long, uid: String, jobUid: String, scheduleUid: String, retryId: Int, taskTrackerNode: String, state: String, eventTime: Long, message: Option[String] = None, updateTime: java.sql.Timestamp)
  /** GetResult implicit for fetching TasksRow objects using plain SQL queries */
  implicit def GetResultTasksRow(implicit e0: GR[Long], e1: GR[String], e2: GR[Int], e3: GR[Option[String]], e4: GR[java.sql.Timestamp]): GR[TasksRow] = GR{
    prs => import prs._
    TasksRow.tupled((<<[Long], <<[String], <<[String], <<[String], <<[Int], <<[String], <<[String], <<[Long], <<?[String], <<[java.sql.Timestamp]))
  }
  /** Table description of table tasks. Objects of this class serve as prototypes for rows in queries. */
  class Tasks(_tableTag: Tag) extends profile.api.Table[TasksRow](_tableTag, Some("akka_job"), "tasks") {
    def * = (rowId, uid, jobUid, scheduleUid, retryId, taskTrackerNode, state, eventTime, message, updateTime) <> (TasksRow.tupled, TasksRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (Rep.Some(rowId), Rep.Some(uid), Rep.Some(jobUid), Rep.Some(scheduleUid), Rep.Some(retryId), Rep.Some(taskTrackerNode), Rep.Some(state), Rep.Some(eventTime), message, Rep.Some(updateTime)).shaped.<>({r=>import r._; _1.map(_=> TasksRow.tupled((_1.get, _2.get, _3.get, _4.get, _5.get, _6.get, _7.get, _8.get, _9, _10.get)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column row_id SqlType(BIGINT), AutoInc, PrimaryKey */
    val rowId: Rep[Long] = column[Long]("row_id", O.AutoInc, O.PrimaryKey)
    /** Database column uid SqlType(VARCHAR), Length(64,true) */
    val uid: Rep[String] = column[String]("uid", O.Length(64,varying=true))
    /** Database column job_uid SqlType(VARCHAR), Length(64,true) */
    val jobUid: Rep[String] = column[String]("job_uid", O.Length(64,varying=true))
    /** Database column schedule_uid SqlType(VARCHAR), Length(64,true) */
    val scheduleUid: Rep[String] = column[String]("schedule_uid", O.Length(64,varying=true))
    /** Database column retry_id SqlType(INT) */
    val retryId: Rep[Int] = column[Int]("retry_id")
    /** Database column task_tracker_node SqlType(VARCHAR), Length(512,true) */
    val taskTrackerNode: Rep[String] = column[String]("task_tracker_node", O.Length(512,varying=true))
    /** Database column state SqlType(VARCHAR), Length(16,true) */
    val state: Rep[String] = column[String]("state", O.Length(16,varying=true))
    /** Database column event_time SqlType(BIGINT) */
    val eventTime: Rep[Long] = column[Long]("event_time")
    /** Database column message SqlType(VARCHAR), Length(1024,true), Default(None) */
    val message: Rep[Option[String]] = column[Option[String]]("message", O.Length(1024,varying=true), O.Default(None))
    /** Database column update_time SqlType(TIMESTAMP) */
    val updateTime: Rep[java.sql.Timestamp] = column[java.sql.Timestamp]("update_time")

    /** Uniqueness Index over (jobUid,scheduleUid,retryId,state) (database name UK_TASK_INSTANCE) */
    val index1 = index("UK_TASK_INSTANCE", (jobUid, scheduleUid, retryId, state), unique=true)
    /** Uniqueness Index over (uid) (database name UK_TASK_UID) */
    val index2 = index("UK_TASK_UID", uid, unique=true)
  }
  /** Collection-like TableQuery object for table Tasks */
  lazy val Tasks = new TableQuery(tag => new Tasks(tag))
}
