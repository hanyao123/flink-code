package com.atguigu.day02

import org.apache.flink.api.common.functions.MapFunction
import org.apache.flink.streaming.api.scala._

object MapFunctionExample {
  def main(args: Array[String]): Unit = {
    val env: StreamExecutionEnvironment = StreamExecutionEnvironment.getExecutionEnvironment
    env.setParallelism(1)
    val stream = env.addSource(new SensorSource)
    stream.map(f => f.id)

    stream.map(new MyMapFunction)

    stream.map(new MapFunction[SensorReading,String] {
      override def map(t: SensorReading): String = t.id
    }).print()

    env.execute()
  }
  class MyMapFunction extends MapFunction[SensorReading, String] {
    override def map(t: SensorReading): String = t.id
  }
}
