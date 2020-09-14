package com.atguigu.day03;

import org.apache.flink.streaming.api.functions.source.SourceFunction;

import java.util.Random;

public class SmokeLevelSource implements SourceFunction<SmokeLevel> {
    private boolean running = true;
    @Override
    public void run(SourceContext<SmokeLevel> sourceContext) throws Exception {
        Random rand = new Random();

        while (running) {
            if (rand.nextGaussian() > 0.8) {
                sourceContext.collect(SmokeLevel.High);
            } else {
                sourceContext.collect(SmokeLevel.Low);
            }
        }
    }

    @Override
    public void cancel() {
        running = false;
    }
}
