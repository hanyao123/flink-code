package com.atguigu.day02

import org.apache.flink.api.common.functions.ReduceFunction
import org.apache.flink.streaming.api.scala._

object KeyedStreamReduceExample {
  def main(args: Array[String]): Unit = {
    val env: StreamExecutionEnvironment = StreamExecutionEnvironment.getExecutionEnvironment
    env.setParallelism(1)

    val stream = env.addSource(new SensorSource).filter(f =>
    f.id == "sensor_1"
    )
    stream.map(r => (r.id, r.temaperature))
   .keyBy(r => r._1)
   .reduce((r1, r2) => (r1._1, r1._2.max(r2._2)))

    stream
      .map(r => (r.id, r.temaperature))
      .keyBy(r => r._1)
      .reduce(new MyReduceFunction)
      .print()

    env.execute()
  }

  class MyReduceFunction extends ReduceFunction[(String, Double)] {
    override def reduce(value1: (String, Double), value2: (String, Double)): (String, Double) =
      (value1._1, value1._2.max(value2._2))
  }
}
