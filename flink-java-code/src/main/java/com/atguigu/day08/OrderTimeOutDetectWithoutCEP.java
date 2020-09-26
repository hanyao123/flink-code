package com.atguigu.day08;

import org.apache.flink.api.common.eventtime.SerializableTimestampAssigner;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.state.ValueState;
import org.apache.flink.api.common.state.ValueStateDescriptor;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.TimeCharacteristic;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.KeyedProcessFunction;
import org.apache.flink.util.Collector;

public class OrderTimeOutDetectWithoutCEP {
    public static void main(String[] args) throws Exception{
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setStreamTimeCharacteristic(TimeCharacteristic.EventTime);
        env.setParallelism(1);

        DataStream<OrderEvent> stream = env
                .fromElements(
                        new OrderEvent("order_1", "create", 2000L),
                        new OrderEvent("order_2", "create", 3000L),
                        new OrderEvent("order_1", "pay", 4000L),
                        new OrderEvent("order_2", "pay", 10000L),
                        new OrderEvent("order_3", "create", 11000L)
                )
                .assignTimestampsAndWatermarks(
                        WatermarkStrategy.<OrderEvent>forMonotonousTimestamps()
                                .withTimestampAssigner(new SerializableTimestampAssigner<OrderEvent>() {
                                    @Override
                                    public long extractTimestamp(OrderEvent orderEvent, long l) {
                                        return orderEvent.orderTime;
                                    }
                                })
                );
        stream
                .keyBy(r -> r.orderId)
                .process(new KeyedProcessFunction<String, OrderEvent, String>() {

                    private ValueState<OrderEvent> orderState;

                    @Override
                    public void open(Configuration parameters) throws Exception {
                        super.open(parameters);
                        orderState = getRuntimeContext().getState(
                                new ValueStateDescriptor<OrderEvent>("order-state", OrderEvent.class)
                        );
                    }

                    @Override
                    public void processElement(OrderEvent orderEvent, Context context, Collector<String> collector) throws Exception {
                        if (orderEvent.orderType.equals("create")) {
                            if (orderState.value() == null) {
                                context.timerService().registerEventTimeTimer(orderEvent.orderTime + 5000L);
                                orderState.update(orderEvent);
                            } else {
                                collector.collect(orderEvent.orderId + " is payed");
                            }
                        } else {
                            orderState.update(orderEvent);//每更新一次状态，状态都会不同吗？
                            // 还是说只有create，pay两种状态来回切换
                            collector.collect(orderEvent.orderId + " is payed");
                        }
                    }

                    @Override
                    public void onTimer(long timestamp, OnTimerContext ctx, Collector<String> out) throws Exception {
                        super.onTimer(timestamp, ctx, out);
                        if (orderState.value() != null && orderState.value().orderType.equals("create")) {
                            out.collect(ctx.getCurrentKey() + " is not payed");
                        }
                        orderState.clear();
                    }
                })
                .print();

        env.execute();
    }
}
