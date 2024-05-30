package com.valterc.ki2.views.battery;

import android.graphics.Matrix;

public class VerticalTopRenderer extends BaseRenderer {

    private final HorizontalLeftRenderer leftRenderer;
    private final Matrix matrixMirror;

    public VerticalTopRenderer() {
        leftRenderer = new HorizontalLeftRenderer();
        matrixMirror = new Matrix();
    }

    @Override
    public void updateSettings(BatteryView batteryView) {
        super.updateSettings(batteryView);
        leftRenderer.updateSettings(batteryView);
    }

    protected void generatePaths(BatteryView batteryView, int internalWidth, int internalHeight) {
        leftRenderer.generatePaths(batteryView, internalWidth, internalHeight);

        pathBackground.set(leftRenderer.pathBackground);
        pathValue.set(leftRenderer.pathValue);
        pathBorder.set(leftRenderer.pathBorder);

        float scaleX = (float) internalWidth / internalHeight;
        float scaleY = (float) internalHeight / internalWidth;

        matrixMirror.reset();
        matrixMirror.postRotate(90, (float) internalWidth / 2, (float) internalHeight / 2);
        matrixMirror.postScale(scaleX, scaleY, (float) internalWidth / 2, (float) internalHeight / 2);

        pathBackground.transform(matrixMirror);
        pathBorder.transform(matrixMirror);
        pathValue.transform(matrixMirror);
    }

}
