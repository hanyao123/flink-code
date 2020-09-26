package com.atguigu.test

import org.apache.flink.streaming.api.scala._
import org.apache.flink.streaming.api.windowing.time.Time

object FlinkToStreamingToScala {

  /** Main program method */
  def main(args: Array[String]) : Unit = {

    val env = StreamExecutionEnvironment.getExecutionEnvironment
    // 并行度设置为1，所有计算都在同一个分区执行
    env.setParallelism(1)

    //val stream = env.fromElements("hello word", "hello world")
    val stream = env.socketTextStream("localhost", 9999, '\n')
      // 使用空格分割字符串 `\\s`表示空格
    stream.flatMap(w => w.split("\\s"))
      // 相当于MapReduce中的Map操作
      .map(w => WordCount(w, 1))
      // shuffle操作
      .keyBy(_.word).timeWindow(Time.seconds(5))
      // 聚合`count`字段
      .sum(1)
      .print()

    env.execute()
  }

  case class WordCount(word: String, count: Int)
}
