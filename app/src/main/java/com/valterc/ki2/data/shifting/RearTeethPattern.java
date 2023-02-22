package com.valterc.ki2.data.shifting;

import com.valterc.ki2.utils.ArrayUtils;

import java.util.Arrays;

public enum RearTeethPattern {

    /*
     * 10 Speed
     */

    S10_P11_43(32, new int[]{11, 13, 15, 17, 20, 23, 26, 30, 36, 43}, "11-43"),

    /*
     * 11 Speed
     */

    S11_P11_40(0, new int[]{11, 13, 15, 17, 19, 21, 24, 27, 31, 35, 40}, "11-40"),
    S11_P11_42(1, new int[]{11, 13, 15, 17, 19, 21, 24, 28, 32, 37, 42}, "11-42"),
    S11_P11_25(2, new int[]{11, 12, 13, 14, 15, 16, 17, 19, 21, 23, 25}, "11-25"),
    S11_P11_28(3, new int[]{11, 12, 13, 14, 15, 17, 19, 21, 23, 25, 28}, "11-28"),
    S11_P11_30(4, new int[]{11, 12, 13, 14, 15, 17, 19, 21, 24, 27, 30}, "11-30"),
    S11_P12_25(5, new int[]{12, 13, 14, 15, 16, 17, 18, 19, 21, 23, 25}, "12-25"),
    S11_P12_28(6, new int[]{12, 13, 14, 15, 16, 17, 19, 21, 23, 25, 28}, "12-28"),
    S11_P11_23(7, new int[]{11, 12, 13, 14, 15, 16, 17, 18, 19, 21, 23}, "11-23"),
    S11_P11_32(8, new int[]{11, 12, 13, 14, 16, 18, 20, 22, 25, 28, 32}, "11-32"),
    S11_P14_28(9, new int[]{14, 15, 16, 17, 18, 19, 20, 21, 23, 25, 28}, "14-28"),
    S11_P11_46(10, new int[]{11, 13, 15, 17, 19, 21, 24, 28, 32, 37, 46}, "11-46"),
    S11_P11_34(11, new int[]{11, 13, 15, 17, 19, 21, 23, 25, 27, 30, 34}, "11-34"),
    S11_P11_50(12, new int[]{11, 13, 15, 17, 20, 23, 26, 30, 36, 43, 50}, "11-50"),

    /*
     * 12 Speed
     */

    S12_P11_28(16, new int[]{11, 12, 13, 14, 15, 16, 17, 18, 19, 21, 24, 28}, "11-28"),
    S12_P11_30(17, new int[]{11, 12, 13, 14, 15, 16, 17, 19, 21, 24, 27, 30}, "11-30"),
    S12_P11_34(18, new int[]{11, 12, 13, 14, 15, 17, 19, 21, 24, 27, 30, 34}, "11-34"),
    S12_P14_34(19, new int[]{14, 15, 16, 17, 18, 19, 20, 22, 24, 27, 30, 34}, "14-34"),
    S12_P11_36(20, new int[]{11, 12, 13, 14, 15, 17, 19, 21, 24, 28, 32, 36}, "11-36"),
    S12_P10_51(21, new int[]{10, 12, 14, 16, 18, 21, 24, 28, 33, 39, 45, 51}, "10-51"),

    UNKNOWN(255, new int[0], "Unknown");

    public static RearTeethPattern fromId(int id) {
        for (RearTeethPattern p : values()) {
            if (p.id == id) {
                return p;
            }
        }

        return UNKNOWN;
    }

    private final int id;
    private final int[] gearTeethCount;
    private final String name;

    RearTeethPattern(int id, int[] gearTeethCount, String name) {
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

        if (gearTeethCount.length - gearIndex < 0) {
            return gearTeethCount[0];
        }

        return gearTeethCount[gearTeethCount.length - gearIndex];
    }

    public int getGearCount() {
        return gearTeethCount.length;
    }

    public int[] getGears() {
        return ArrayUtils.reverse(Arrays.copyOf(gearTeethCount, gearTeethCount.length));
    }

    public String getName() {
        return name;
    }

}
