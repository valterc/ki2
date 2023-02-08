package com.valterc.ki2.utils;

public final class ArrayUtils {

    /**
     * Reverses the contents of an array in-place.
     *
     * @param array Array that will be reversed in-place.
     * @return Reversed array in-place.
     */
    public static int[] reverse(int[] array) {
        for (int i = 0; i < array.length / 2; i++) {
            int temp = array[i];
            array[i] = array[array.length - i - 1];
            array[array.length - i - 1] = temp;
        }

        return array;
    }
}
