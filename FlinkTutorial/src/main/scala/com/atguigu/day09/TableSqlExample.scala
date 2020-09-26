package com.atguigu.day09

import com.atguigu.day02.{SensorSource}
import org.apache.flink.streaming.api.scala.StreamExecutionEnvironment
import org.apache.flink.table.api._
import org.apache.flink.table.api.bridge.scala.StreamTableEnvironment
import org.apache.flink.types.Row
import org.apache.flink.api.scala._

object TableSqlExample {
  def main(args: Array[String]): Unit = {
    val env = StreamExecutionEnvironment.getExecutionEnvironment
    env.setParallelism(1)
//设置环境
    val settings: EnvironmentSettings = EnvironmentSettings.newInstance().inStreamingMode().build()
    //创建表
    val tableEnv: StreamTableEnvironment = StreamTableEnvironment.create(env,settings)

    val stream = env.addSource(new SensorSource)

    val table: Table = tableEnv.fromDataStream(stream)

    val table1: Table = table.filter($"id" === "sensor_1")
      .select($"id", $"temaperature")
    tableEnv.toAppendStream[Row](table1).print()
    tableEnv.createTemporaryView("sensor", stream)
    // tableEnv.createTemporaryView("sensor", stream, $"id", $"timestamp" as "ts", $"temperature" as "temp")

    val sqlResult = tableEnv
      .sqlQuery("SELECT * FROM sensor where id = 'sensor_2'")

    tableEnv
      // Table => DataStream
      .toAppendStream[Row](sqlResult)
      .print()
    env.execute()
  }
}
