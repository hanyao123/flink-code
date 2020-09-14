package com.atguigu.day02


import org.apache.flink.streaming.api.functions.co.CoFlatMapFunction
import org.apache.flink.streaming.api.scala._
import org.apache.flink.util.Collector

object CoFlatMapFunctionExample {
  def main(args: Array[String]): Unit = {

    val env: StreamExecutionEnvironment = StreamExecutionEnvironment.getExecutionEnvironment
    env.setParallelism(1)
    val stream1: KeyedStream[(Int, String), Int] = env.fromElements((1, "aaaa"), (2, "bbbb"))
      .keyBy(r => r._1)

    val stream2: KeyedStream[(Int, String), Int] = env.fromElements((1, "cccc"), (2, "dddd"))
      .keyBy(r => r._1)

    val connected: ConnectedStreams[(Int, String), (Int, String)] = stream1.connect(stream2)
    connected.flatMap(new MyCoFlatMapFunction).print()
    env.execute()
  }
  class MyCoFlatMapFunction extends CoFlatMapFunction[(Int, String), (Int, String), String] {

     var v: String = _

    override def flatMap1(value: (Int, String), out: Collector[String]): Unit = {
      out.collect(value._2 + " 来自第一条流的元素发送两次")
      out.collect(value._2 + " 来自第一条流的元素发送两次")
      if (v != null) {
        out.collect(v + value._2)
      } else {
        v = value._2
      }
    }

    override def flatMap2(value: (Int, String), out: Collector[String]): Unit = {
      out.collect(value._2 + " 来自第二条流的元素发送一次")
      if (v != null) {
        out.collect(v + value._2)
      } else {
        v = value._2
      }
    }
  }
}