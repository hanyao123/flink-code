package com.atguigu.day05

import com.atguigu.day02.{SensorReading, SensorSource}
import org.apache.flink.api.common.state.{ValueState, ValueStateDescriptor}
import org.apache.flink.api.scala.typeutils.Types
import org.apache.flink.streaming.api.functions.co.CoProcessFunction
import org.apache.flink.streaming.api.scala._
import org.apache.flink.util.Collector

object SensorSwitch {
  def main(args: Array[String]): Unit = {
    val env: StreamExecutionEnvironment = StreamExecutionEnvironment.getExecutionEnvironment
    env.setParallelism(1)
    val stream: DataStream[SensorReading] = env.addSource(new SensorSource).keyBy(r =>r.id)

    val switches: KeyedStream[(String, Long), String] = env.fromElements(("sensor_2", 10 * 1000L)).keyBy(r => r._1)

    stream.connect(switches)
        .process(new SwitchProcess)
      .print()

    env.execute()
  }
class SwitchProcess extends CoProcessFunction[SensorReading,(String,Long),SensorReading]{
  lazy val forwardSwitch: ValueState[Boolean] = getRuntimeContext.getState(new ValueStateDescriptor[Boolean]("switch",Types.of[Boolean]))

  override def processElement1(in1: SensorReading, context: CoProcessFunction[SensorReading, (String, Long), SensorReading]#Context, collector: Collector[SensorReading]): Unit = {
if(forwardSwitch.value()){
  collector.collect(in1)
}
  }

  override def processElement2(in2: (String, Long), context: CoProcessFunction[SensorReading, (String, Long), SensorReading]#Context, collector: Collector[SensorReading]): Unit = {
forwardSwitch.update(true)
    context.timerService().registerProcessingTimeTimer(context.timerService().currentProcessingTime()+in2._2)
  }

  override def onTimer(timestamp: Long, ctx: CoProcessFunction[SensorReading, (String, Long), SensorReading]#OnTimerContext, out: Collector[SensorReading]): Unit = {
forwardSwitch.clear()
  }
}
}
