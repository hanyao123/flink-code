package com.atguigu.day02;

import org.apache.flink.api.common.functions.FilterFunction;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

import java.util.logging.Filter;

public class FilterFunctionExample {
    public static void main(String[] args) throws Exception{
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(1);

        DataStreamSource<SensorReading> stream = env.addSource(new SensorSource());
        stream.filter(f -> f.temperature > 0);

        stream.filter(new MyFliterFilFunction());

        stream.filter(new FilterFunction<SensorReading>() {
            @Override
            public boolean filter(SensorReading sensorReading) throws Exception {
                return sensorReading.temperature > 0;
            }
        }).print();
        env.execute();


    }
    public static class MyFliterFilFunction implements FilterFunction<SensorReading>
    {
        @Override
        public boolean filter(SensorReading sensorReading) throws Exception {
            return sensorReading.temperature>0;
        }
    }
}
