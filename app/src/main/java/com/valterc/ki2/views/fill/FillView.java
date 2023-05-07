package com.valterc.ki2.views.fill;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Picture;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;

import androidx.annotation.ColorInt;
import androidx.annotation.Nullable;

import com.valterc.ki2.R;

public class FillView extends View {

    private static final float[] CORNER_RADIUS_FILL = new float[]{2, 2, 0, 0, 0, 0, 2, 2};

    private final boolean initialized;
    private Picture picture;
    private float value;
    private int colorForeground;

    public FillView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        TypedArray array = context.getTheme().obtainStyledAttributes(attrs, R.styleable.FillView, 0, 0);
        try {
            setValue(array.getFloat(R.styleable.FillView_value, 0));

            TypedValue colorValue = new TypedValue();
            context.getTheme().resolveAttribute(android.R.attr.textColorPrimary, colorValue, true);
            int defaultColor = colorValue.data;

            setForegroundColor(array.getColor(R.styleable.FillView_foregroundColor, defaultColor));
        } finally {
            array.recycle();
        }

        initialized = true;
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        drawPicture();
    }

    private void drawPicture() {
        picture = new Picture();
        Canvas canvas = picture.beginRecording(getWidth(), getHeight());

        int internalWidth = getWidth() - (getPaddingStart() + getPaddingEnd());
        int internalHeight = getHeight() - (getPaddingTop() + getPaddingBottom());

        RectF rect = new RectF(getPaddingStart(), getPaddingTop(),getPaddingStart() + internalWidth, getPaddingTop() + internalHeight);

        Path path = new Path();
        path.addRoundRect(rect, CORNER_RADIUS_FILL, Path.Direction.CW);

        canvas.clipPath(path);

        Paint paintForeground = new Paint(Paint.FILTER_BITMAP_FLAG);
        paintForeground.setStyle(Paint.Style.FILL);
        paintForeground.setColor(colorForeground);

        canvas.drawRect(getPaddingStart(), getPaddingTop(),getPaddingStart() + internalWidth * value, getPaddingTop() + internalHeight, paintForeground);

        picture.endRecording();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (picture != null) {
            canvas.drawPicture(picture);
        }
    }

    @ColorInt
    public int getForegroundColor() {
        return colorForeground;
    }

    public void setForegroundColor(Color color) {
        setForegroundColor(color.toArgb());
    }

    public void setForegroundColor(@ColorInt int color) {
        if (this.colorForeground == color) {
            return;
        }

        colorForeground = color;

        if (initialized) {
            invalidate();
            requestLayout();
        }
    }

    public float getValue() {
        return value;
    }

    public void setValue(float value) {
        if (this.value == value) {
            return;
        }

        this.value = Math.min(1, Math.max(0, value));

        if (initialized) {
            invalidate();
            requestLayout();
        }
    }

}
