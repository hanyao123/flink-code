package com.atguigu.day08

import java.util.Properties

import org.apache.flink.streaming.api.environment._
import org.apache.flink.streaming.connectors.kafka.{FlinkKafkaConsumer011, FlinkKafkaProducer011}
import org.apache.flink.streaming.util.serialization.SimpleStringSchema

object FlinkKafkaProducerExample {
  def main(args: Array[String]): Unit = {
    val env = StreamExecutionEnvironment.getExecutionEnvironment
    env.setParallelism(1)

    val conProperties = new Properties()
    conProperties.setProperty("bootstrap.servers", "hadoop102:9092")
    conProperties.setProperty("group.id", "consumer-group")
    conProperties.setProperty("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer")
    conProperties.setProperty("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer")
    conProperties.setProperty("auto.offset.reset", "latest")

    val stream = env.addSource(new FlinkKafkaConsumer011[String](
      "test",
      new SimpleStringSchema(),
      conProperties
    ))

    val prodProperties = new Properties()
    prodProperties.setProperty("bootstrap.servers", "hadoop102:9092")

    stream
      .addSink(new FlinkKafkaProducer011[String](
        "test",
        new SimpleStringSchema(),
        prodProperties
      ))
//为什么要定义addSink，直接在addSource后面print不好吗？
    stream.print()

    env.execute()
  }
}
