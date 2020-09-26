package com.atguigu.day07

import org.apache.flink.cep.scala.{CEP, PatternStream}
import org.apache.flink.cep.scala.pattern.Pattern
import org.apache.flink.streaming.api.TimeCharacteristic
import org.apache.flink.streaming.api.scala._
import org.apache.flink.streaming.api.windowing.time.Time
import org.apache.flink.util.Collector

object OrderTimeoutDetect {
  def main(args: Array[String]): Unit = {
    val env: StreamExecutionEnvironment = StreamExecutionEnvironment.getExecutionEnvironment
    env.setParallelism(1)
    env.setStreamTimeCharacteristic(TimeCharacteristic.EventTime)

    val timeoutTag = new OutputTag[String]("timeout-tag")

    val stream: DataStream[OrderEvent] = env.fromElements(
      OrderEvent("order_1", "create", 2000L),
      OrderEvent("order_2", "create", 3000L),
      OrderEvent("order_1", "pay", 4000L)
    ).assignAscendingTimestamps(_.eventTime).keyBy(r => r.orderId)
    val pattern: Pattern[OrderEvent, OrderEvent] = Pattern.begin[OrderEvent]("creat").where(_.eventType.equals("creat"))
      .next("pay").where(_.eventType.equals("pay"))
      .within(Time.seconds(5))

    val patternStream: PatternStream[OrderEvent] = CEP.pattern(stream,pattern)

    val selectFunc = (map: scala.collection.Map[String, Iterable[OrderEvent]], out: Collector[String]) => {
      val create = map("create").iterator.next()
      out.collect("order id " + create.orderId + " is payed!")
    }

    val timeoutFunc = (map: scala.collection.Map[String, Iterable[OrderEvent]], ts: Long, out: Collector[String]) => {
      val create = map("create").iterator.next()
      out.collect("order id " + create.orderId + " is not payed! and timeout ts is " + ts)
    }
    val selectStream = patternStream.flatSelect(timeoutTag)(timeoutFunc)(selectFunc)

    selectStream.print()
    selectStream.getSideOutput(timeoutTag).print()

    env.execute()
  }
case class OrderEvent(orderId: String, eventType: String, eventTime: Long)
}
