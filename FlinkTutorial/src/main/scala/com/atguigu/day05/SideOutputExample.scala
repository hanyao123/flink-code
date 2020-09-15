package com.atguigu.day05

import com.atguigu.day02.{SensorReading, SensorSource}
import org.apache.flink.streaming.api.functions.ProcessFunction
import org.apache.flink.streaming.api.scala._
import org.apache.flink.util.Collector

object SideOutputExample {
  private val sideOutput = new OutputTag[String]("side-output")
  def main(args: Array[String]): Unit = {
    val env: StreamExecutionEnvironment = StreamExecutionEnvironment.getExecutionEnvironment
    env.setParallelism(1)
    val stream: DataStream[SensorReading] = env.addSource(new SensorSource)
val wearing = stream.process(new FreezingAlarm)
    wearing.print()
    wearing.getSideOutput(sideOutput).print()
    env.execute()
  }
  class FreezingAlarm extends ProcessFunction[SensorReading, SensorReading]{
    override def processElement(i: SensorReading, context: ProcessFunction[SensorReading, SensorReading]#Context, collector: Collector[SensorReading]): Unit = {
      if (i.temaperature < 32.0){
        context.output(sideOutput, "传感器ID为：" + i.id + "的传感器温度小于32度！")
      }
      collector.collect(i)
    }
  }
}
