package com.atguigu.day08;

public class OrderEvent{
    public String orderId;
    public String orderType;
    public Long orderTime;

    public OrderEvent(String orderId, String orderType, Long orderTime) {
        this.orderId = orderId;
        this.orderType = orderType;
        this.orderTime = orderTime;
    }

    public OrderEvent() {
    }

    @Override
    public String toString() {
        return "OrderEvent{" +
                "orderId='" + orderId + '\'' +
                ", orderType='" + orderType + '\'' +
                ", orderTime=" + orderTime +
                '}';
    }
}
