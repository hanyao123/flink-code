package com.atguigu.day09;

import org.apache.flink.api.common.eventtime.SerializableTimestampAssigner;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.functions.AggregateFunction;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.common.state.ListState;
import org.apache.flink.api.common.state.ListStateDescriptor;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.TimeCharacteristic;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.KeyedProcessFunction;
import org.apache.flink.streaming.api.functions.windowing.ProcessWindowFunction;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;
import org.apache.flink.util.Collector;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Comparator;

public class UserBehaviorAnalys {
    public static void main(String[] args) throws Exception{
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(1);
        env.setStreamTimeCharacteristic(TimeCharacteristic.EventTime);

        DataStream<UserBehavior> stream = env.readTextFile(
                "E:\\flinkProject\\FlinkTutorial\\src\\main\\resources\\UserBehavior.csv")
        .map(new MapFunction<String, UserBehavior>() {
            @Override
            public UserBehavior map(String line) throws Exception {
                String[] split = line.split(",");
                return new UserBehavior(split[0],split[1],split[2],split[3],Long.parseLong(split[4]));
            }
        }).filter(f -> f.brhavior.equals("pv"))
                .assignTimestampsAndWatermarks(
                        WatermarkStrategy.<UserBehavior>forMonotonousTimestamps()
                        .withTimestampAssigner(new SerializableTimestampAssigner<UserBehavior>() {
                            @Override
                            public long extractTimestamp(UserBehavior element, long recordTimestamp) {
                                return element.timestamp;
                            }
                        })
                );

        stream.keyBy(r -> r.itermId)
                .timeWindow(Time.hours(1),Time.minutes(5))
                .aggregate(new Myaggratetion(),new Mywindows())
                .keyBy(r -> r.windowEnd)
                .process(new MyTopN(3))
                .print();
        env.execute();
    }

    public static class MyTopN extends KeyedProcessFunction<Long,ItermViewCount,String>{
         private ListState<ItermViewCount> listState;
         private Integer topN;
          public MyTopN(Integer topN){
              this.topN = topN;
          }

        @Override
        public void open(Configuration parameters) throws Exception {
            super.open(parameters);
            listState =getRuntimeContext().getListState(new ListStateDescriptor<ItermViewCount>("listState",ItermViewCount.class));
        }

        @Override
        public void processElement(ItermViewCount value, Context ctx, Collector<String> out) throws Exception {
            listState.add(value);
            ctx.timerService().registerProcessingTimeTimer(value.windowEnd + 1000L);
        }

        @Override
        public void onTimer(long timestamp, OnTimerContext ctx, Collector<String> out) throws Exception {
            super.onTimer(timestamp, ctx, out);
            ArrayList<ItermViewCount> itermViewCounts = new ArrayList<>();
            for(ItermViewCount iterm : listState.get()){
                itermViewCounts.add(iterm);
            }
            listState.clear();
            itermViewCounts.sort(new Comparator<ItermViewCount>() {
                @Override
                public int compare(ItermViewCount o1, ItermViewCount o2) {
                    return o2.count.intValue() - o1.count.intValue();
                }
            });
            StringBuffer stringBuffer = new StringBuffer();
            stringBuffer
                    .append("=============================================================\n");
            stringBuffer
                    .append("time: ")
                    .append(new Timestamp(timestamp - 1000L))
                    .append("\n");

            for (int i = 0; i < this.topN; i++) {
                ItermViewCount item = itermViewCounts.get(i);
                stringBuffer
                        .append("No.")
                        .append(i + 1)
                        .append(" : ")
                        .append(" itemID = ")
                        .append(item.itermId)
                        .append(" count = ")
                        .append(item.count)
                        .append("\n");
            }
            stringBuffer
                    .append("=============================================================\n\n");
            Thread.sleep(1000L);
            out.collect(stringBuffer.toString());
        }
    }

    public static class Myaggratetion implements AggregateFunction<UserBehavior,Long,Long> {

        @Override
        public Long createAccumulator() {
            return 0L;
        }

        @Override
        public Long add(UserBehavior value, Long accumulator) {
            return accumulator + 1;
        }

        @Override
        public Long getResult(Long accumulator) {
            return accumulator;
        }

        @Override
        public Long merge(Long a, Long b) {
            return null;
        }
    }

        public static class Mywindows extends ProcessWindowFunction<Long,ItermViewCount,String, TimeWindow>{

            @Override
            public void process(String key, Context context, Iterable<Long> elements, Collector<ItermViewCount> out) throws Exception {
                out.collect(new ItermViewCount(key,context.window().getEnd(),elements.iterator().next()));
            }
        }

}
