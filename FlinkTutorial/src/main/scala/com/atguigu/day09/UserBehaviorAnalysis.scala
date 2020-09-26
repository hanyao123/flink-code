package com.atguigu.day09

import java.sql.Timestamp

import org.apache.flink.api.common.functions.AggregateFunction
import org.apache.flink.api.common.state.ListStateDescriptor
import org.apache.flink.api.scala.typeutils.Types
import org.apache.flink.streaming.api.TimeCharacteristic
import org.apache.flink.streaming.api.functions.KeyedProcessFunction
import org.apache.flink.streaming.api.scala._
import org.apache.flink.streaming.api.scala.function.ProcessWindowFunction
import org.apache.flink.streaming.api.windowing.time.Time
import org.apache.flink.streaming.api.windowing.windows.TimeWindow
import org.apache.flink.util.Collector

import scala.collection.JavaConversions._
import scala.collection.mutable.ListBuffer

object UserBehaviorAnalysis {
  def main(args: Array[String]): Unit = {
    val env = StreamExecutionEnvironment.getExecutionEnvironment
    env.setParallelism(1)
    env.setStreamTimeCharacteristic(TimeCharacteristic.EventTime)

    val stream = env.readTextFile("E:\\flinkProject\\FlinkTutorial\\src\\main\\resources\\UserBehavior.csv")
      .map(line => {
        val splits: Array[String] = line.split(",")
        new UserBehavior(splits(0), splits(1), splits(2), splits(3), splits(4).toLong * 1000L)
      }).filter(r =>r.bahavior.equals("pv"))
      .assignAscendingTimestamps(_.timestamp)

    stream.keyBy(r => r.itemId)
      .timeWindow(Time.hours(1),Time.minutes(5))
      .aggregate(new MyAgg,new MyWin)
        .keyBy(r => r.windowEnd)
        .process(new topN(3))


    env.execute()
  }
  class topN(topN: Int) extends KeyedProcessFunction[Long,ItemViewCount,String]{
     lazy val listState = getRuntimeContext.getListState(new ListStateDescriptor[ItemViewCount]("listState",Types.of[ItemViewCount]))
    override def processElement(value: ItemViewCount, ctx: KeyedProcessFunction[Long, ItemViewCount, String]#Context, out: Collector[String]): Unit = {
      listState.add(value)
      ctx.timerService().registerEventTimeTimer(value.windowEnd +1L)
    }
    override def onTimer(timestamp: Long, ctx: _root_.org.apache.flink.streaming.api.functions.KeyedProcessFunction[Long, _root_.com.atguigu.day09.UserBehaviorAnalysis.ItemViewCount, _root_.scala.Predef.String]#OnTimerContext, out: _root_.org.apache.flink.util.Collector[_root_.scala.Predef.String]): Unit = {
        super.onTimer(timestamp, ctx, out)
      val itemViewCounts = new ListBuffer[ItemViewCount]()
      for(item <- listState.get()){
        itemViewCounts += item
      }

      listState.clear()

      val sortedItems: ListBuffer[ItemViewCount] = itemViewCounts.sortBy(r => r.count).take(topN)

      val result = new StringBuilder
      result.append("====================================\n")
      result.append("时间: ").append(new Timestamp(timestamp - 1)).append("\n")
      for (i <- sortedItems.indices) {
        val currentItem = sortedItems(i)
        result.append("No")
          .append(i + 1)
          .append(" : ")
          .append("  商品ID = ")
          .append(currentItem.itemId)
          .append("  浏览量 = ")
          .append(currentItem.count)
          .append("\n")
      }
      result.append("====================================\n\n")
      Thread.sleep(1000)
      out.collect(result.toString())
      }
  }


  class MyAgg extends AggregateFunction[UserBehavior,Long,Long]{
    override def createAccumulator(): Long =0L

    override def add(value: UserBehavior, accumulator: Long): Long = accumulator + 1

    override def getResult(accumulator: Long): Long = accumulator

    override def merge(a: Long, b: Long): Long = ???
  }

  class MyWin extends ProcessWindowFunction[Long,ItemViewCount,String,TimeWindow]{
    override def process(key: String, context: Context, elements: Iterable[Long], out: Collector[ItemViewCount]): Unit = {
      out.collect(new ItemViewCount(key,context.window.getEnd,elements.head))
    }
  }
  case class UserBehavior(userId:String,itemId:String,catetoryId:String,bahavior:String,timestamp:Long)
  case class ItemViewCount(itemId:String,windowEnd:Long,count:Long)
}
