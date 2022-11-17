package com.valterc.ki2.utils;

import android.graphics.Color;

import androidx.annotation.ColorInt;
import androidx.annotation.FloatRange;
import androidx.annotation.IntRange;
import androidx.annotation.NonNull;

public final class ColorUtils {

    private ColorUtils() {
    }

    private static final double XYZ_WHITE_REFERENCE_X = 95.047;
    private static final double XYZ_WHITE_REFERENCE_Y = 100;
    private static final double XYZ_WHITE_REFERENCE_Z = 108.883;
    private static final double XYZ_EPSILON = 0.008856;
    private static final double XYZ_KAPPA = 903.3;

    public static void colorToLAB(@ColorInt int color, @NonNull double[] outLab) {
        RGBToLAB(Color.red(color), Color.green(color), Color.blue(color), outLab);
    }

    public static double distanceEuclidean(@NonNull double[] labX, @NonNull double[] labY) {
        return Math.sqrt(Math.pow(labX[0] - labY[0], 2)
                + Math.pow(labX[1] - labY[1], 2)
                + Math.pow(labX[2] - labY[2], 2));
    }

    public static void RGBToLAB(@IntRange(from = 0x0, to = 0xFF) int r,
                                @IntRange(from = 0x0, to = 0xFF) int g, @IntRange(from = 0x0, to = 0xFF) int b,
                                @NonNull double[] outLab) {
        // First we convert RGB to XYZ
        RGBToXYZ(r, g, b, outLab);
        // outLab now contains XYZ
        XYZToLAB(outLab[0], outLab[1], outLab[2], outLab);
        // outLab now contains LAB representation
    }

    public static void RGBToXYZ(@IntRange(from = 0x0, to = 0xFF) int r,
                                @IntRange(from = 0x0, to = 0xFF) int g, @IntRange(from = 0x0, to = 0xFF) int b,
                                @NonNull double[] outXyz) {
        if (outXyz.length != 3) {
            throw new IllegalArgumentException("outXyz must have a length of 3.");
        }

        double sr = r / 255.0;
        sr = sr < 0.04045 ? sr / 12.92 : Math.pow((sr + 0.055) / 1.055, 2.4);
        double sg = g / 255.0;
        sg = sg < 0.04045 ? sg / 12.92 : Math.pow((sg + 0.055) / 1.055, 2.4);
        double sb = b / 255.0;
        sb = sb < 0.04045 ? sb / 12.92 : Math.pow((sb + 0.055) / 1.055, 2.4);

        outXyz[0] = 100 * (sr * 0.4124 + sg * 0.3576 + sb * 0.1805);
        outXyz[1] = 100 * (sr * 0.2126 + sg * 0.7152 + sb * 0.0722);
        outXyz[2] = 100 * (sr * 0.0193 + sg * 0.1192 + sb * 0.9505);
    }

    public static void XYZToLAB(@FloatRange(from = 0f, to = XYZ_WHITE_REFERENCE_X) double x,
                                @FloatRange(from = 0f, to = XYZ_WHITE_REFERENCE_Y) double y,
                                @FloatRange(from = 0f, to = XYZ_WHITE_REFERENCE_Z) double z,
                                @NonNull double[] outLab) {
        if (outLab.length != 3) {
            throw new IllegalArgumentException("outLab must have a length of 3.");
        }
        x = pivotXyzComponent(x / XYZ_WHITE_REFERENCE_X);
        y = pivotXyzComponent(y / XYZ_WHITE_REFERENCE_Y);
        z = pivotXyzComponent(z / XYZ_WHITE_REFERENCE_Z);
        outLab[0] = Math.max(0, 116 * y - 16);
        outLab[1] = 500 * (x - y);
        outLab[2] = 200 * (y - z);
    }

    private static double pivotXyzComponent(double component) {
        return component > XYZ_EPSILON
                ? Math.pow(component, 1 / 3.0)
                : (XYZ_KAPPA * component + 16) / 116;
    }

}
