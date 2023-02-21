package com.valterc.ki2.views.battery;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;

public abstract class BaseRenderer implements IBatteryViewRenderer {

    protected final RectF rect = new RectF();

    protected final Paint paintBorder;
    protected final Paint paintBackground;
    protected final Paint paintForeground;

    protected final Path pathBorder;
    protected final Path pathValue;
    protected final Path pathBackground;

    public BaseRenderer() {
        paintBorder = new Paint(Paint.ANTI_ALIAS_FLAG);
        paintBorder.setStyle(Paint.Style.STROKE);
        paintBorder.setStrokeWidth(1);

        paintBackground = new Paint(Paint.FILTER_BITMAP_FLAG);
        paintBackground.setStyle(Paint.Style.FILL);

        paintForeground = new Paint(Paint.FILTER_BITMAP_FLAG);
        paintForeground.setStyle(Paint.Style.FILL);

        pathBorder = new Path();
        pathValue = new Path();
        pathBackground = new Path();
    }

    protected abstract void generatePaths(BatteryView batteryView, int internalWidth, int internalHeight);

    @Override
    public void render(BatteryView batteryView, Canvas canvas, int internalWidth, int internalHeight) {
        generatePaths(batteryView, internalWidth, internalHeight);

        paintBorder.setColor(batteryView.getBorderColor());
        paintBackground.setColor(batteryView.getBackgroundColor());
        paintForeground.setColor(batteryView.getForegroundColor());

        canvas.drawPath(pathBackground, paintBackground);
        canvas.drawPath(pathValue, paintForeground);

        paintBorder.setStyle(Paint.Style.STROKE);
        canvas.drawPath(pathBackground, paintBorder);

        paintBorder.setStyle(Paint.Style.FILL_AND_STROKE);
        canvas.drawPath(pathBorder, paintBorder);
    }

}
