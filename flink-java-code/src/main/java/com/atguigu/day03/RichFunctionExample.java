package com.atguigu.day03;

import org.apache.flink.api.common.functions.RichMapFunction;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;


public class RichFunctionExample {
    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(1);

        DataStream<Integer> stream = env.fromElements(1, 2, 3);
stream.map(new RichMapFunction<Integer, Integer>() {

    @Override
    public Integer map(Integer integer) throws Exception {
        return integer+1;
    }
}).print();

env.execute();
    }


}
