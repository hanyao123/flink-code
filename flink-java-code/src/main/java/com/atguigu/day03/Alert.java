package com.atguigu.day03;

public class Alert {
    private String message;
    private Long timeStamp;

    public Alert() {
    }

    public Alert(String message, Long timeStamp) {
        this.message = message;
        this.timeStamp = timeStamp;
    }

    @Override
    public String toString() {
        return "{" + message + "," + timeStamp + '}';
    }
}
