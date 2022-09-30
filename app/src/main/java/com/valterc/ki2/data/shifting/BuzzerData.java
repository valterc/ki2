package com.valterc.ki2.data.shifting;

public class BuzzerData {

    public static final long BUZZER_ON_TIME_MS = 2000;

    private int sequenceNumber;
    private long time;

    public BuzzerData(){
    }

    public int getSequenceNumber() {
        return sequenceNumber;
    }

    public void setSequenceNumber(int sequenceNumber){
        this.sequenceNumber = sequenceNumber;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public void resetTime() {
        this.time = 0;
    }

    public boolean isExpired(){
        return System.currentTimeMillis() - time > BUZZER_ON_TIME_MS;
    }

}
