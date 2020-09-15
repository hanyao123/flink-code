package com.atguigu.day05;

import com.atguigu.day02.SensorReading;
import com.atguigu.day02.SensorSource;
import org.apache.commons.math3.fitting.leastsquares.EvaluationRmsChecker;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.ProcessFunction;
import org.apache.flink.util.Collector;
import org.apache.flink.util.OutputTag;

public class SideOutputExample {
    private static OutputTag<String> sideOutput = new OutputTag<String>("side-output"){};
    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(1);
        DataStream<SensorReading> stream = env.addSource(new SensorSource());
        SingleOutputStreamOperator<SensorReading> waring = stream.process(new FreezingAlarm());
waring.print();
waring.getSideOutput(sideOutput).print();
env.execute();
    }
public static class FreezingAlarm extends ProcessFunction<SensorReading, SensorReading>{
    @Override
    public void processElement(SensorReading sensorReading, Context context, Collector<SensorReading> collector) throws Exception {
        if (sensorReading.temperature < 32.0) {
            context.output(sideOutput, "温度小于32度！");
        }
        collector.collect(sensorReading);
    }
    }
}
