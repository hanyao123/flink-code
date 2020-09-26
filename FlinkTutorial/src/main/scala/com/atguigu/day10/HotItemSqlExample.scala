package com.atguigu.day10

import org.apache.flink.streaming.api.TimeCharacteristic
import org.apache.flink.streaming.api.scala.StreamExecutionEnvironment
import org.apache.flink.api.scala._
import org.apache.flink.table.api._
import org.apache.flink.table.api.bridge.scala.StreamTableEnvironment
import org.apache.flink.types.Row

object HotItemSqlExample {
  def main(args: Array[String]): Unit = {
    val env = StreamExecutionEnvironment.getExecutionEnvironment
    env.setParallelism(1)
    env.setStreamTimeCharacteristic(TimeCharacteristic.EventTime)

    val settings = EnvironmentSettings
      .newInstance()
      .inStreamingMode()
      .build()

    val tableEnv: StreamTableEnvironment = StreamTableEnvironment.create(env,settings)

    val stream= env.readTextFile("E:\\flinkProject\\FlinkTutorial\\src\\main\\resources\\UserBehavior.csv")
      .map(line => {
        val strings: Array[String] = line.split(",")
         UserBehavior(strings(0), strings(1), strings(2), strings(3), strings(4).toLong * 1000L)
      }).filter(r => r.behavior.equals("pv"))
      .assignAscendingTimestamps(r => r.timestamp)

    tableEnv.createTemporaryView("t", stream, $"itemId", $"timestamp".rowtime() as "ts")
    val result = tableEnv
      .sqlQuery(
        """
          |SELECT *
          |FROM (
          |    SELECT *, ROW_NUMBER() OVER (PARTITION BY windowEnd ORDER BY itemCount DESC) as row_num
          |    FROM (SELECT itemId, COUNT(itemId) as itemCount, HOP_END(ts, INTERVAL '5' MINUTE, INTERVAL '1' HOUR) as windowEnd
          |          FROM t GROUP BY HOP(ts, INTERVAL '5' MINUTE, INTERVAL '1' HOUR), itemId)
          |)
          |WHERE row_num <= 3
          |""".stripMargin)
    tableEnv.toRetractStream[Row](result).print()

    env.execute()
  }
  case class UserBehavior(userId: String, itemId: String, categoryId: String, behavior: String, timestamp: Long)
}
