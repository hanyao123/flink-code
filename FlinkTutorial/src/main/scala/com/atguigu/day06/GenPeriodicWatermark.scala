package com.atguigu.day06

import org.apache.flink.streaming.api.TimeCharacteristic
import org.apache.flink.streaming.api.functions.{AssignerWithPeriodicWatermarks, KeyedProcessFunction, ProcessFunction}
import org.apache.flink.streaming.api.scala._
import org.apache.flink.streaming.api.watermark.Watermark
import org.apache.flink.util.Collector

object GenPeriodicWatermark {
  def main(args: Array[String]): Unit = {
    val env: StreamExecutionEnvironment = StreamExecutionEnvironment.getExecutionEnvironment
    env.setParallelism(1)
    env.setStreamTimeCharacteristic(TimeCharacteristic.EventTime)
    env.getConfig.setAutoWatermarkInterval(10*1000L)

    val stream: DataStream[String] = env.socketTextStream("localhost",9999,'\n')

    stream.map(line =>{
      val arr: Array[String] = line.split(" ")
      (arr(0),arr(1).toLong *1000L)
    }).assignTimestampsAndWatermarks(
      new AssignerWithPeriodicWatermarks[(String, Long)] {
        val bound = 10 * 1000L // 最大延迟时间
        var maxTs = Long.MinValue + bound // 当前观察到的最大时间戳
        // 用来生成水位线
        // 默认200ms调用一次
        override def getCurrentWatermark: Watermark = {
          println("generate watermark!!!" + (maxTs - bound - 1) + "ms")
          new Watermark(maxTs - bound - 1)
        }
        // 每来一条数据都会调用一次
        override def extractTimestamp(element: (String, Long), recordTimestamp: Long): Long = {
          println("extract timestamp!!!")
          maxTs = maxTs.max(element._2) // 更新观察到的最大事件时间
          element._2 // 抽取时间戳
        }
      }
    ).keyBy(r => r._1).process(new KeyedProcessFunction[String,(String,Long),String] {
      override def processElement(value: (String, Long), ctx: KeyedProcessFunction[String, (String, Long), String]#Context, out: Collector[String]): Unit = {
        out.collect("current watermark is " + ctx.timerService().currentWatermark())
      }
    }
    )
    env.execute()
  }
}
