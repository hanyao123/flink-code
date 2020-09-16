package com.atguigu.day06

import com.atguigu.day02.{SensorReading, SensorSource}
import org.apache.flink.runtime.state.filesystem.FsStateBackend
import org.apache.flink.streaming.api.scala._

object StateBackendExample {
  def main(args: Array[String]): Unit = {
    val env: StreamExecutionEnvironment = StreamExecutionEnvironment.getExecutionEnvironment
    env.setParallelism(1)
env.setStateBackend(new FsStateBackend("file:///input11"))
env.enableCheckpointing(10*1000L)
    val stream: DataStream[SensorReading] = env.addSource(new SensorSource)
    stream.print()
    env.execute()
  }
}
