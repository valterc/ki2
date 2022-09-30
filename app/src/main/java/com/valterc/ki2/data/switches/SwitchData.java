package com.valterc.ki2.data.switches;

public class SwitchData {

    private int sequenceNumber;
    private int repeat;

    public SwitchData(){
        this.sequenceNumber = -1;
        this.repeat = 0;
    }

    public int getSequenceNumber() {
        return sequenceNumber;
    }

    public void setSequenceNumber(int sequenceNumber){
        this.sequenceNumber = sequenceNumber;
    }

    public int getRepeat() {
        return repeat;
    }

    public void incrementRepeat() {
        this.repeat++;
    }

    public void resetRepeat() {
        this.repeat = 0;
    }
}
