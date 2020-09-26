package com.atguigu.day11

import com.atguigu.day02.SensorSource
import org.apache.flink.streaming.api.scala._
import org.apache.flink.table.api._
import org.apache.flink.table.api.EnvironmentSettings
import org.apache.flink.table.api.bridge.scala.StreamTableEnvironment
import org.apache.flink.table.functions.AggregateFunction
import org.apache.flink.types.Row

object AggregateFunction {
  def main(args: Array[String]): Unit = {
    val env = StreamExecutionEnvironment.getExecutionEnvironment
    env.setParallelism(1)

    val settings = EnvironmentSettings.newInstance().inStreamingMode().build()

    val tableEnv = StreamTableEnvironment.create(env, settings)

    val stream = env.addSource(new SensorSource)

    val temp = new AvgTemp()

    // sql
    tableEnv.createTemporaryView("sensor", stream)
    tableEnv.registerFunction("avgTemp", temp)
    val sqlResult = tableEnv.sqlQuery("SELECT id, avgTemp(temperature) FROM sensor GROUP BY id")
    tableEnv.toRetractStream[Row](sqlResult).print()

    // table api
    val table = tableEnv.fromDataStream(stream)
    val tableResult = table.groupBy($"id").aggregate(temp($"temperature") as "avgTemp").select($"id", $"avgTemp")
    tableEnv.toRetractStream[Row](tableResult).print()

    env.execute()
  }
  class AvgTemp extends AggregateFunction[Double, AvgTempAcc] {
    override def createAccumulator(): AvgTempAcc = new AvgTempAcc

    override def getValue(acc: AvgTempAcc): Double = acc.sum / acc.count

    def accumulate(acc: AvgTempAcc, in: Double): Unit = {
      acc.sum += in
      acc.count += 1
    }
  }
  class AvgTempAcc {
    var sum: Double = 0.0
    var count: Int = 0
  }
}
