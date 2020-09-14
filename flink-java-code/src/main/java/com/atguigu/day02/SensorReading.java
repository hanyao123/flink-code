package com.atguigu.day02;

public class SensorReading {
    public String id;
    public long timestamp;
    public double temperature;
    public SensorReading(){ }
    public SensorReading( String id,long timestamp,double temperature){
        this.id=id;
        this.temperature = temperature;
        this.timestamp=timestamp;
    }

    public String toString(){
        return "("+ this.id + ","+this.timestamp + ","+this.temperature+")";
    }
}
