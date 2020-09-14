package com.atguigu.day02;

import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

public class MapFunctionExample {
    public static void main(String[] args) throws Exception{
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(1);

        DataStreamSource<SensorReading> stream = env.addSource(new SensorSource());
        stream.map(f->f.id);

        stream.map(new MyMapFunction());

        stream.map(new MapFunction<SensorReading, String>() {
            @Override
            public String map(SensorReading sensorReading) throws Exception {
                return sensorReading.id;
            }
        }).print();
        env.execute();


    }
    public static class MyMapFunction implements MapFunction<SensorReading, String>
    {
        @Override
        public String map(SensorReading sensorReading) throws Exception {
            return sensorReading.id;
        }
    }
}
