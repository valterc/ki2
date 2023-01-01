package com.valterc.ki2.data.shifting;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

public class RearTeethPatternTest {

    @Test
    public void S11_P11_32() {
        RearTeethPattern rearTeethPattern = RearTeethPattern.S11_P11_32;

        Assertions.assertEquals(11, rearTeethPattern.getGearCount());

        Assertions.assertEquals(32, rearTeethPattern.getTeethCount(1));
        Assertions.assertEquals(28, rearTeethPattern.getTeethCount(2));

        Assertions.assertEquals(12, rearTeethPattern.getTeethCount(rearTeethPattern.getGearCount() - 1));
        Assertions.assertEquals(11, rearTeethPattern.getTeethCount(rearTeethPattern.getGearCount()));
    }

    @Test
    public void S12_P11_30() {
        RearTeethPattern rearTeethPattern = RearTeethPattern.S12_P11_30;

        Assertions.assertEquals(12, rearTeethPattern.getGearCount());

        Assertions.assertEquals(30, rearTeethPattern.getTeethCount(1));
        Assertions.assertEquals(27, rearTeethPattern.getTeethCount(2));

        Assertions.assertEquals(12, rearTeethPattern.getTeethCount(rearTeethPattern.getGearCount() - 1));
        Assertions.assertEquals(11, rearTeethPattern.getTeethCount(rearTeethPattern.getGearCount()));
    }

    @ParameterizedTest
    @EnumSource(RearTeethPattern.class)
    public void getTeethCount_CorrectOrder(RearTeethPattern rearTeethPattern) {
        int firstGearTeethCount = rearTeethPattern.getTeethCount(1);
        int lastGearTeethCount = rearTeethPattern.getTeethCount(rearTeethPattern.getGearCount());

        Assertions.assertTrue(firstGearTeethCount >= lastGearTeethCount);
    }

    @ParameterizedTest
    @EnumSource(RearTeethPattern.class)
    public void getTeethCount_LowerEdge(RearTeethPattern rearTeethPattern) {
        int index0TeethCount = rearTeethPattern.getTeethCount(0);
        int index1TeethCount = rearTeethPattern.getTeethCount(1);

        Assertions.assertEquals(index0TeethCount, index1TeethCount);
    }

    @ParameterizedTest
    @EnumSource(RearTeethPattern.class)
    public void getTeethCount_UpperEdge(RearTeethPattern rearTeethPattern) {
        int indexCount0TeethCount = rearTeethPattern.getTeethCount(rearTeethPattern.getGearCount());
        int indexCount1TeethCount = rearTeethPattern.getTeethCount(rearTeethPattern.getGearCount() + 1);

        Assertions.assertEquals(indexCount0TeethCount, indexCount1TeethCount);
    }

}
