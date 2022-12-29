package com.valterc.ki2.data.shifting;

public class ShiftingInfoBuilder {

    private BuzzerType buzzerType;
    private int frontGear;
    private int frontGearMax;
    private int rearGear;
    private int rearGearMax;
    private ShiftingMode shiftingMode;

    private boolean buzzerTypeSet;
    private boolean frontGearSet;
    private boolean frontGearMaxSet;
    private boolean rearGearSet;
    private boolean rearGearMaxSet;
    private boolean shiftingModeSet;

    public ShiftingInfoBuilder() {
    }

    public BuzzerType getBuzzerType() {
        return buzzerType;
    }

    public void setBuzzerType(BuzzerType buzzerType) {
        this.buzzerType = buzzerType;
        this.buzzerTypeSet = true;
    }

    public int getFrontGear() {
        return frontGear;
    }

    public void setFrontGear(int frontGear) {
        this.frontGear = frontGear;
        this.frontGearSet = true;
    }

    public int getFrontGearMax() {
        return frontGearMax;
    }

    public void setFrontGearMax(int frontGearMax) {
        this.frontGearMax = frontGearMax;
        this.frontGearMaxSet = true;
    }

    public int getRearGear() {
        return rearGear;
    }

    public void setRearGear(int rearGear) {
        this.rearGear = rearGear;
        this.rearGearSet = true;
    }

    public int getRearGearMax() {
        return rearGearMax;
    }

    public void setRearGearMax(int rearGearMax) {
        this.rearGearMax = rearGearMax;
        this.rearGearMaxSet = true;
    }

    public ShiftingMode getShiftingMode() {
        return shiftingMode;
    }

    public void setShiftingMode(ShiftingMode shiftingMode) {
        this.shiftingMode = shiftingMode;
        this.shiftingModeSet = true;
    }

    public boolean allSet(){
        return buzzerTypeSet &&
                frontGearSet &&
                frontGearMaxSet &&
                rearGearSet &&
                rearGearMaxSet &&
                shiftingModeSet;
    }

    public ShiftingInfo build(){
        return new ShiftingInfo(buzzerType, frontGear, frontGearMax, rearGear, rearGearMax, shiftingMode);
    }
}
