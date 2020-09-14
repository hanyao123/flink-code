package com.atguigu.day02

import org.apache.flink.api.common.functions.{FilterFunction, MapFunction}
import org.apache.flink.streaming.api.scala._

object FilterFunctionExample {
  def main(args: Array[String]): Unit = {
    val env: StreamExecutionEnvironment = StreamExecutionEnvironment.getExecutionEnvironment
    env.setParallelism(1)
    val stream = env.addSource(new SensorSource)

    stream.filter(t => t.temaperature>0)

    stream.filter(new MyFilterFunction)

    stream.filter(new FilterFunction[SensorReading] {
      override def filter(t: SensorReading): Boolean = t.temaperature > 0
    }).print()

    env.execute()
  }
  class MyFilterFunction extends FilterFunction[SensorReading] {
    override def filter(t: SensorReading): Boolean = t.temaperature > 0
  }
}
