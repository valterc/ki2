package com.valterc.ki2.views.battery;

import android.graphics.Matrix;

public class HorizontalRightRenderer extends BaseRenderer {

    private final HorizontalLeftRenderer leftRenderer;
    private final Matrix matrixMirror;

    public HorizontalRightRenderer() {
        leftRenderer = new HorizontalLeftRenderer();
        matrixMirror = new Matrix();
    }

    protected void generatePaths(BatteryView batteryView, int internalWidth, int internalHeight) {
        leftRenderer.generatePaths(batteryView, internalWidth, internalHeight);

        pathBackground.set(leftRenderer.pathBackground);
        pathValue.set(leftRenderer.pathValue);
        pathBorder.set(leftRenderer.pathBorder);

        matrixMirror.reset();
        matrixMirror.postScale(-1, 1, (float) internalWidth / 2, 0);

        pathBackground.transform(matrixMirror);
        pathBorder.transform(matrixMirror);
        pathValue.transform(matrixMirror);
    }

}
