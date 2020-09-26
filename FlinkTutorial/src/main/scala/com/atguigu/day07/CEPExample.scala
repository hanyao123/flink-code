package com.atguigu.day07

import org.apache.flink.cep.scala.{CEP, PatternStream}
import org.apache.flink.cep.scala.pattern.Pattern
import org.apache.flink.streaming.api.TimeCharacteristic
import org.apache.flink.streaming.api.scala._
import org.apache.flink.streaming.api.windowing.time.Time

object CEPExample {
  def main(args: Array[String]): Unit = {
    val env = StreamExecutionEnvironment.getExecutionEnvironment
    env.setParallelism(1)
    env.setStreamTimeCharacteristic(TimeCharacteristic.EventTime)

    val stream = env
      .fromElements(
        LoginEvent("user_1", "192.168.0.1", "fail", 2000L),
        LoginEvent("user_1", "192.168.0.2", "fail", 3000L),
        LoginEvent("user_1", "192.168.0.3", "fail", 4000L),
        LoginEvent("user_2", "192.168.10.10", "success", 5000L)
      )
      .assignAscendingTimestamps(_.eventTime)
      .keyBy(r => r.userId)

    val pattern = Pattern
      .begin[LoginEvent]("first")
      .where(r => r.eventType.equals("fail"))
      .next("second")
      .where(r => r.eventType.equals("fail"))
      .next("third")
      .where(r => r.eventType.equals("fail"))
      .within(Time.seconds(5))

val parrentStream: PatternStream[LoginEvent] = CEP.pattern(stream,pattern)
    parrentStream.select((pattern:scala.collection.Map[String,Iterable[LoginEvent]])=>{
      val first = pattern("first").iterator.next()
      val second = pattern("second").iterator.next()
      val third = pattern("third").iterator.next()
      (first.userId,first.ipAddress,second.ipAddress,third.ipAddress)
    }).print()
    env.execute()
  }
case class LoginEvent(userId:String,ipAddress:String,eventType:String,eventTime:Long)
}
