package com.atguigu.day05

import org.apache.flink.streaming.api.TimeCharacteristic
import org.apache.flink.streaming.api.scala._
import org.apache.flink.streaming.api.scala.function.ProcessWindowFunction
import org.apache.flink.streaming.api.windowing.time.Time
import org.apache.flink.streaming.api.windowing.windows.TimeWindow
import org.apache.flink.util.Collector

object RedirectLateEvent {
  def main(args: Array[String]): Unit = {
    val env: StreamExecutionEnvironment = StreamExecutionEnvironment.getExecutionEnvironment
    env.setParallelism(1)
    env.setStreamTimeCharacteristic(TimeCharacteristic.EventTime)

    val stream: DataStream[String] = env.socketTextStream("localhost",9999,'\n')

    val readings = stream
      .map(line => {
        val arr = line.split(" ")
        (arr(0), arr(1).toLong * 1000L)
      })
      .assignAscendingTimestamps(_._2)
      .keyBy(r => r._1)
      .timeWindow(Time.seconds(5))
      .sideOutputLateData(new OutputTag[(String, Long)]("late-readings"))
      .process(new CountWindow)

    readings.print()
    readings.getSideOutput(new OutputTag[(String, Long)]("late-readings")).print()

    env.execute()
  }
  class CountWindow extends ProcessWindowFunction[(String, Long), String, String, TimeWindow] {
    override def process(key: String, context: Context, elements: Iterable[(String, Long)], out: Collector[String]): Unit = {
      out.collect("一共有" + elements.size + " 个元素")
    }
  }
}
