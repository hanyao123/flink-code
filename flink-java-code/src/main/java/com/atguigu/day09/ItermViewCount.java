package com.atguigu.day09;

import java.sql.Timestamp;

public class ItermViewCount {
    public String itermId;
    public Long windowEnd;
    public Long count;

    public ItermViewCount(String itermId, Long windowEnd, Long count) {
        this.itermId = itermId;
        this.windowEnd = windowEnd;
        this.count = count;
    }

    public ItermViewCount() {
    }

    @Override
    public String toString() {
        return "ItermViewCount{" +
                "itermId='" + itermId + '\'' +
                ", windowEnd=" + new Timestamp(windowEnd) +
                ", count=" + count +
                '}';
    }
}
