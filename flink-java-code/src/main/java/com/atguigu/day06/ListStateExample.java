package com.atguigu.day06;

import com.atguigu.day02.SensorReading;
import com.atguigu.day02.SensorSource;
import org.apache.flink.api.common.state.ListState;
import org.apache.flink.api.common.state.ListStateDescriptor;
import org.apache.flink.api.common.state.ValueState;
import org.apache.flink.api.common.state.ValueStateDescriptor;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.KeyedProcessFunction;
import org.apache.flink.util.Collector;

public class ListStateExample {
    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(1);

         env.addSource(new SensorSource())
                .filter(r -> r.id.equals("sensor_1"))
                .keyBy(r -> r.id)
                .process(new KeyedProcessFunction<String, SensorReading, String>() {
                    private ListState<SensorReading> reading;
                    private ValueState<Long> timeTs;
                    @Override
                    public void open(Configuration parameters) throws Exception {
                        super.open(parameters);
                        reading = getRuntimeContext()
                                .getListState(new ListStateDescriptor<SensorReading>("reading-state",SensorReading.class));
                        timeTs = getRuntimeContext()
                                .getState(new ValueStateDescriptor<Long>("time-state",Long.class));
                    }

                    @Override
                    public void processElement(SensorReading value, Context ctx, Collector<String> out) throws Exception {
                        reading.add(value);
                        if(timeTs.value() == null){
                            ctx.timerService().registerProcessingTimeTimer(ctx.timerService().currentProcessingTime() + 10 * 1000L);
                            timeTs.update(1L);
                        }
                    }

                    @Override
                    public void onTimer(long timestamp, OnTimerContext ctx, Collector<String> out) throws Exception {
                        super.onTimer(timestamp, ctx, out);
                        long count = 0L;
                        for(SensorReading r : reading.get()) {
                            count++;
                        }
                        out.collect("there are " + count + " readings");
                        out.collect(ctx.timerService().currentProcessingTime()+" ");
                       timeTs.clear();
                    }
                }).print();

        env.execute();
    }
}