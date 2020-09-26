package com.atguigu.day09;

public class UserBehavior {
    public String userId;
    public String itermId;
    public String categoryId;
    public String brhavior;
    public Long timestamp;

    public UserBehavior(String userId, String itermId, String categoryId, String brhavior, Long timestamp) {
        this.userId = userId;
        this.itermId = itermId;
        this.categoryId = categoryId;
        this.brhavior = brhavior;
        this.timestamp = timestamp;
    }

    public UserBehavior() {
    }

    @Override
    public String toString() {
        return "UserBehavior{" +
                "userId='" + userId + '\'' +
                ", itermId='" + itermId + '\'' +
                ", categoryId='" + categoryId + '\'' +
                ", brhavior='" + brhavior + '\'' +
                ", timestamp=" + timestamp +
                '}';
    }
}
