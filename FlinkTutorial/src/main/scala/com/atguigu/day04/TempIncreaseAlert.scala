package com.atguigu.day04

import com.atguigu.day02.{SensorReading, SensorSource}
import org.apache.flink.api.common.state.{ValueState, ValueStateDescriptor}
import org.apache.flink.api.scala.typeutils.Types
import org.apache.flink.streaming.api.functions.{KeyedProcessFunction, ProcessFunction}
import org.apache.flink.streaming.api.scala._
import org.apache.flink.util.Collector

object TempIncreaseAlert {
  def main(args: Array[String]): Unit = {
    val env: StreamExecutionEnvironment = StreamExecutionEnvironment.getExecutionEnvironment
    env.setParallelism(1)
    val stream: DataStream[SensorReading] = env.addSource(new SensorSource)
    stream
      .keyBy(r => r.id)
      .process(new TempIncrease)
      .print()
    env.execute()
  }
class TempIncrease extends KeyedProcessFunction[String,SensorReading,String]{
  // 懒加载；
  // 状态变量会在检查点操作时进行持久化，例如hdfs
  // 只会初始化一次，单例模式
  // 在当机重启程序时，首先去持久化设备寻找名为`last-temp`的状态变量，如果存在，则直接读取。不存在，则初始化。
  // 用来保存最近一次温度
  // 默认值是0.0
  lazy val lastTemp: ValueState[Double] = getRuntimeContext.getState(
    new ValueStateDescriptor[Double]("last-temp", Types.of[Double])
  )

  // 默认值是0L
  lazy val timer: ValueState[Long] = getRuntimeContext.getState(
    new ValueStateDescriptor[Long]("timer", Types.of[Long])
  )
  override def processElement(i: SensorReading, context: KeyedProcessFunction[String, SensorReading, String]#Context, collector: Collector[String]): Unit = {
    // 使用`.value()`方法取出最近一次温度值，如果来的温度是第一条温度，则prevTemp为0.0
    val prevTemp = lastTemp.value()
    // 将到来的这条温度值存入状态变量中
    lastTemp.update(i.temaperature)

    // 如果timer中有定时器的时间戳，则读取
    val ts = timer.value()

    if (prevTemp == 0.0 || i.temaperature < prevTemp) {
      context.timerService().deleteProcessingTimeTimer(ts)
      timer.clear()
    } else if (i.temaperature > prevTemp && ts == 0) {
      val oneSecondLater = context.timerService().currentProcessingTime() + 1000L
      context.timerService().registerProcessingTimeTimer(oneSecondLater)
      timer.update(oneSecondLater)
    }
  }

  override def onTimer(timestamp: Long, ctx: KeyedProcessFunction[String, SensorReading, String]#OnTimerContext, out: Collector[String]): Unit = {
    out.collect("传感器ID是 " + ctx.getCurrentKey + " 的传感器的温度连续1s上升了！")
    timer.clear()
  }
}
}
