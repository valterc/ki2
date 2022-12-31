package com.valterc.ki2.data.shifting;

public enum FrontTeethPattern {

    P38_28(0, new int[]{38, 28}, "38-28"),
    P36_26(1, new int[]{36, 26}, "36-26"),
    P34_24(2, new int[]{34, 24}, "34-24"),
    P40_30_22(3, new int[]{40, 30, 22}, "40-30-22"),
    P52_39(4, new int[]{52, 39}, "52-39"),
    P52_36(5, new int[]{52, 36}, "52-36"),
    P50_34(6, new int[]{50, 34}, "50-34"),
    P54_42(7, new int[]{54, 42}, "54-42"),
    P55_42(8, new int[]{55, 42}, "55-42"),
    P53_39(9, new int[]{53, 39}, "53-39"),
    P52_38(10, new int[]{52, 38}, "52-38"),
    P46_36(11, new int[]{46, 36}, "46-36"),
    P48_31(12, new int[]{48, 31}, "48-31"),
    P46_30(13, new int[]{46, 30}, "46-30"),
    P54_40(14, new int[]{54, 40}, "54-40"),

    UNKNOWN(255, new int[0], "Unknown");

    public static FrontTeethPattern fromId(int id) {
        for (FrontTeethPattern p : values()) {
            if (p.id == id) {
                return p;
            }
        }

        return UNKNOWN;
    }

    private final int id;
    private final int[] gearTeethCount;
    private final String name;

    FrontTeethPattern(int id, int[] gearTeethCount, String name) {
        this.id = id;
        this.gearTeethCount = gearTeethCount;
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public Integer getTeethCount(int gearIndex) {
        if (gearTeethCount.length == 0) {
            return 0;
        }

        if (gearTeethCount.length - gearIndex > gearTeethCount.length - 1) {
            return gearTeethCount[gearTeethCount.length - 1];
        }

        if (gearIndex < 1) {
            return gearTeethCount[0];
        }

        return gearTeethCount[gearTeethCount.length - gearIndex];
    }

    public int getGearCount() {
        return gearTeethCount.length;
    }

    public String getName() {
        return name;
    }
}
