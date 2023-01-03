package com.valterc.ki2.data.shifting;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

public class FrontTeethPatternTest {

    @Test
    public void P50_34() {
        FrontTeethPattern frontTeethPattern = FrontTeethPattern.P50_34;

        Assertions.assertEquals(2, frontTeethPattern.getGearCount());

        Assertions.assertEquals(34, frontTeethPattern.getTeethCount(1));
        Assertions.assertEquals(50, frontTeethPattern.getTeethCount(2));
    }

    @Test
    public void P52_38() {
        FrontTeethPattern frontTeethPattern = FrontTeethPattern.P52_38;

        Assertions.assertEquals(2, frontTeethPattern.getGearCount());

        Assertions.assertEquals(38, frontTeethPattern.getTeethCount(1));
        Assertions.assertEquals(52, frontTeethPattern.getTeethCount(2));
    }

    @Test
    public void P40_30_22() {
        FrontTeethPattern frontTeethPattern = FrontTeethPattern.P40_30_22;

        Assertions.assertEquals(3, frontTeethPattern.getGearCount());

        Assertions.assertEquals(22, frontTeethPattern.getTeethCount(1));
        Assertions.assertEquals(30, frontTeethPattern.getTeethCount(2));
        Assertions.assertEquals(40, frontTeethPattern.getTeethCount(3));
    }

    @ParameterizedTest
    @EnumSource(FrontTeethPattern.class)
    public void getTeethCount_CorrectOrder(FrontTeethPattern frontTeethPattern) {
        int firstGearTeethCount = frontTeethPattern.getTeethCount(1);
        int lastGearTeethCount = frontTeethPattern.getTeethCount(frontTeethPattern.getGearCount());

        Assertions.assertTrue(firstGearTeethCount <= lastGearTeethCount);
    }

    @ParameterizedTest
    @EnumSource(FrontTeethPattern.class)
    public void getTeethCount_LowerEdge(FrontTeethPattern frontTeethPattern) {
        int index0TeethCount = frontTeethPattern.getTeethCount(0);
        int index1TeethCount = frontTeethPattern.getTeethCount(1);

        Assertions.assertEquals(index0TeethCount, index1TeethCount);
    }

    @ParameterizedTest
    @EnumSource(FrontTeethPattern.class)
    public void getTeethCount_UpperEdge(FrontTeethPattern frontTeethPattern) {
        int indexCount0TeethCount = frontTeethPattern.getTeethCount(frontTeethPattern.getGearCount());
        int indexCount1TeethCount = frontTeethPattern.getTeethCount(frontTeethPattern.getGearCount() + 1);

        Assertions.assertEquals(indexCount0TeethCount, indexCount1TeethCount);
    }

}
