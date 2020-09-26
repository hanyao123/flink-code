package com.atguigu.day09

import com.atguigu.day02.{SensorReading, SensorSource}
import org.apache.flink.streaming.api.scala._
import org.apache.flink.table.api._
import org.apache.flink.table.api.bridge.scala.StreamTableEnvironment
import org.apache.flink.types.Row

object RetractStreamExample {
  def main(args: Array[String]): Unit = {
    val env: StreamExecutionEnvironment = StreamExecutionEnvironment.getExecutionEnvironment
    env.setParallelism(1)

    val settings: EnvironmentSettings = EnvironmentSettings.newInstance().inStreamingMode().build()
    val stream: DataStream[SensorReading] = env.addSource(new SensorSource)
    val tableEnv: StreamTableEnvironment = StreamTableEnvironment.create(env,settings)
    val result = tableEnv.sqlQuery(
      """
        |select id,count(id)
        |from sensor
        |where id = "sensor_1"
        |group by id
        |""".stripMargin)

    tableEnv.toRetractStream[Row](result).print()
    //table api

    val table: Table = tableEnv.fromDataStream(stream)
    table.filter($"id" === "sensor_1").groupBy("id")
      .select($"id",$"id".count())
    tableEnv.toRetractStream[Row](table).print()

    env.execute()
  }

}
