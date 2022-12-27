package com.valterc.ki2.data.shifting;

public class BuzzerData {

    public static final long TIME_MS_BUZZER_ON = 500;

    private int sequenceNumber;
    private long time;

    public BuzzerData() {
    }

    public int getSequenceNumber() {
        return sequenceNumber;
    }

    public void setSequenceNumber(int sequenceNumber) {
        this.sequenceNumber = sequenceNumber;
        this.time = System.currentTimeMillis();
    }

    public void resetTime() {
        this.time = 0;
    }

    public boolean isExpired() {
        return System.currentTimeMillis() - time > TIME_MS_BUZZER_ON;
    }

}
