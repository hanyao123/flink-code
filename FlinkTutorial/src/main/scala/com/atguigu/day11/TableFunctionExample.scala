package com.atguigu.day11

import org.apache.flink.streaming.api.scala._
import org.apache.flink.table.annotation.{DataTypeHint, FunctionHint}
import org.apache.flink.table.api._
import org.apache.flink.table.api.{EnvironmentSettings, Table}
import org.apache.flink.table.api.bridge.scala.StreamTableEnvironment
import org.apache.flink.table.functions.TableFunction
import org.apache.flink.types.Row

object TableFunctionExample {
  def main(args: Array[String]): Unit = {
    val env: StreamExecutionEnvironment = StreamExecutionEnvironment.getExecutionEnvironment
    env.setParallelism(1)

   val stream = env.fromElements("hello#world", "atguigu#bigdata")

    val settings = EnvironmentSettings.newInstance().inStreamingMode().build()

    val tableEnv = StreamTableEnvironment.create(env,settings)
    tableEnv.createTemporaryView("Mytable",stream,$"s")
    tableEnv.createTemporarySystemFunction("SplitFunction",classOf[SplitFunction])

    //table api
    val tableStream: Table = tableEnv.from("Mytable")
      .joinLateral(call("SplitFunction",$"s"))
      .select($"s",$"word",$"length")
    tableEnv.toAppendStream[Row](tableStream).print()

    //sql
    val table: Table = tableEnv.sqlQuery("select s,word, length from Mytable,lateral table(SplitFunction(s))")
    tableEnv.toAppendStream[Row](table).print()

    env.execute()
  }

  @FunctionHint(output = new DataTypeHint("ROW<word STRING, length INT>"))
  class SplitFunction extends TableFunction[Row] {
    def eval(string: String): Unit = {
      string.split("#").foreach(s => collect(Row.of(s, Int.box(s.length))))
    }
  }
}
