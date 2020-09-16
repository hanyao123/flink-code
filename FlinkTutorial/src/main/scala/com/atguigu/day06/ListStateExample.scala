package com.atguigu.day06

import com.atguigu.day02.{SensorReading, SensorSource}
import org.apache.flink.api.common.state.{ListStateDescriptor, ValueStateDescriptor}
import org.apache.flink.api.scala.typeutils.Types
import org.apache.flink.streaming.api.functions.KeyedProcessFunction
import org.apache.flink.streaming.api.scala._
import org.apache.flink.util.Collector

import scala.collection.mutable.ListBuffer

object ListStateExample {
  def main(args: Array[String]): Unit = {
    val env: StreamExecutionEnvironment = StreamExecutionEnvironment.getExecutionEnvironment
    env.setParallelism(1)

    val stream = env
      .addSource(new SensorSource)
      .filter(r => r.id.equals("sensor_1"))
      .keyBy(r => r.id)
      .process(new Keyed)

    stream.print()
    env.execute()
  }
  class Keyed extends KeyedProcessFunction[String, SensorReading, String]{
    lazy val readingList = getRuntimeContext.getListState(
      new ListStateDescriptor[SensorReading]("list-readings", Types.of[SensorReading])
    )

    lazy val timerTs = getRuntimeContext.getState(
      new ValueStateDescriptor[Long]("ts", Types.of[Long])
    )
    override def processElement(value: SensorReading, ctx: KeyedProcessFunction[String, SensorReading, String]#Context, out: Collector[String]): Unit = {
      readingList.add(value)
      if (timerTs.value() == 0L) {
        ctx.timerService().registerProcessingTimeTimer(ctx.timerService().currentProcessingTime() + 10 * 1000L)
        timerTs.update(ctx.timerService().currentProcessingTime() + 10 * 1000L)
      }
    }

    override def onTimer(timestamp: Long, ctx: KeyedProcessFunction[String, SensorReading, String]#OnTimerContext, out: Collector[String]): Unit = {
      val arr = new ListBuffer[SensorReading]()
      import scala.collection.JavaConversions._
      for (r <- readingList.get()) {
        arr += r
      }
      out.collect("there are " + arr.size + " readings")
      timerTs.clear()
    }
  }
}
