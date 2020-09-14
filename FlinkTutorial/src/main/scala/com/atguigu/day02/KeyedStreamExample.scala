package com.atguigu.day02

import org.apache.flink.streaming.api.scala._
object KeyedStreamExample {
  def main(args: Array[String]): Unit = {
    val env: StreamExecutionEnvironment = StreamExecutionEnvironment.getExecutionEnvironment
    env.setParallelism(1)

    val stream: DataStream[SensorReading] = env.addSource(new SensorSource)
    val streams: DataStream[SensorReading] = stream.filter(f => f.id == "sensor_1")
    val keyedStream: KeyedStream[SensorReading, String] = streams.keyBy(r => r.id)
    keyedStream.max(2).print()
    env.execute()

  }
}
