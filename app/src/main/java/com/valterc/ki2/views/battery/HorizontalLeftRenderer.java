package com.valterc.ki2.views.battery;

import android.graphics.Path;

public class HorizontalLeftRenderer extends BaseRenderer {

    private static final float[] NON_FULL_CORNER_RADIUS = new float[]{0, 0, 2, 2, 2, 2, 0, 0};
    private static final float[] FULL_CORNER_RADIUS = new float[]{2, 2, 2, 2, 2, 2, 2, 2};

    public HorizontalLeftRenderer() {
    }

    protected void generatePaths(BatteryView batteryView, int internalWidth, int internalHeight) {
        pathBackground.reset();
        pathValue.reset();
        pathBorder.reset();

        rect.left = batteryView.getPaddingStart() + Math.max(internalWidth * .1f, 2);
        rect.top = batteryView.getPaddingTop() + paintBorder.getStrokeWidth() / 2;
        rect.right = batteryView.getPaddingStart() + internalWidth - paintBorder.getStrokeWidth() / 2;
        rect.bottom = batteryView.getPaddingTop() + internalHeight - paintBorder.getStrokeWidth() / 2;

        pathBackground.addRoundRect(rect, 2, 2, Path.Direction.CW);

        rect.left = batteryView.getPaddingStart() + Math.max(internalWidth * .1f, 2);
        rect.right = batteryView.getPaddingStart() + internalWidth - paintBorder.getStrokeWidth() / 2;
        rect.left = batteryView.getPaddingStart() + Math.max(internalWidth * .1f, 2) + (rect.right - rect.left) * (1 - batteryView.getValue());

        if (batteryView.getValue() > 0.99) {
            pathValue.addRoundRect(rect, FULL_CORNER_RADIUS, Path.Direction.CW);
        } else {
            pathValue.addRoundRect(rect, NON_FULL_CORNER_RADIUS, Path.Direction.CW);
        }

        rect.left = batteryView.getPaddingStart();
        rect.top = batteryView.getPaddingTop() + internalHeight * .3f;
        rect.right = batteryView.getPaddingStart() + Math.max(internalWidth * .1f, 2);
        rect.bottom = batteryView.getPaddingTop() + internalHeight - internalHeight * .3f;

        pathBackground.addRect(rect, Path.Direction.CW);
        pathBorder.addRect(rect, Path.Direction.CW);
    }

}
