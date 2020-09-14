package com.atguigu.day04

import org.apache.flink.streaming.api.TimeCharacteristic
import org.apache.flink.streaming.api.functions.KeyedProcessFunction
import org.apache.flink.streaming.api.scala._
import org.apache.flink.util.Collector

object KeyedProcessExample {
  def main(args: Array[String]): Unit = {
    val env: StreamExecutionEnvironment = StreamExecutionEnvironment.getExecutionEnvironment
    env.setParallelism(1)
    env.setStreamTimeCharacteristic(TimeCharacteristic.EventTime)

    val stream: DataStream[String] = env.socketTextStream("hadoop102",9999,'\n')

    stream.map(line =>{
      val arr: Array[String] = line.split(" ")
      (arr(0),arr(1).toLong * 1000L)
    }).assignAscendingTimestamps(r => r._2).keyBy(r => r._1)
      .process(new MyKeyed).print()
    env.execute()
  }

class MyKeyed extends KeyedProcessFunction[String,(String,Long),String]{
  override def processElement(i: (String, Long), context: KeyedProcessFunction[String, (String, Long), String]#Context, collector: Collector[String]): Unit = {
    context.timerService().registerEventTimeTimer(i._2 +10 *1000L)
  }

  override def onTimer(timestamp: Long, ctx: KeyedProcessFunction[String, (String, Long), String]#OnTimerContext, out: Collector[String]): Unit = {
    out.collect("定时器触发了！触发时间是："+ timestamp)
  }
}
}
