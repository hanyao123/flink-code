package com.atguigu.day07;

public class LoginEvent {
    public String userId;
    public String ip;
    public String eventType;
    public Long eventTime;

    public LoginEvent() {
    }

    public LoginEvent(String userId, String ip, String eventType, Long eventTime) {
        this.userId = userId;
        this.ip = ip;
        this.eventType = eventType;
        this.eventTime = eventTime;
    }

    @Override
    public String toString() {
        return "LoginEvent{" +
                "userId='" + userId + '\'' +
                ", ip='" + ip + '\'' +
                ", eventType='" + eventType + '\'' +
                ", eventTime=" + eventTime +
                '}';
    }
}
