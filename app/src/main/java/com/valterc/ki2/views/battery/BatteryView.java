package com.valterc.ki2.views.battery;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Picture;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;

import androidx.annotation.ColorInt;
import androidx.annotation.Nullable;

import com.valterc.ki2.R;

@SuppressWarnings("unused")
public class BatteryView extends View {

    public enum Orientation {
        HORIZONTAL_LEFT,
        HORIZONTAL_RIGHT,
        VERTICAL_TOP,
        VERTICAL_BOTTOM
    }

    private final boolean initialized;

    private Picture picture;

    private Orientation orientation;
    private IBatteryViewRenderer renderer;
    private int colorBorder;
    private int colorBackground;
    private int colorForeground;
    private float value;


    public BatteryView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        TypedArray array = context.getTheme().obtainStyledAttributes(attrs, R.styleable.BatteryView, 0, 0);
        try {
            setValue(array.getFloat(R.styleable.BatteryView_value, 0));
            setOrientation(array.getInt(R.styleable.BatteryView_orientation, Orientation.HORIZONTAL_LEFT.ordinal()));

            TypedValue colorValue = new TypedValue();
            context.getTheme().resolveAttribute(android.R.attr.textColorPrimary, colorValue, true);
            int defaultColor = colorValue.data;

            setBorderColor(array.getColor(R.styleable.BatteryView_borderColor, defaultColor));
            setBackgroundColor(array.getColor(R.styleable.BatteryView_backgroundColor, Color.TRANSPARENT));
            setForegroundColor(array.getColor(R.styleable.BatteryView_foregroundColor, defaultColor));
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

        renderer.render(this, canvas, internalWidth, internalHeight);
        picture.endRecording();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (picture != null) {
            canvas.drawPicture(picture);
        }
    }

    public void setBorderColor(Color color) {
        setBorderColor(color.toArgb());
    }

    public void setBorderColor(int color) {
        if (this.colorBorder == color) {
            return;
        }

        colorBorder = color;

        if (initialized) {
            invalidate();
            requestLayout();
        }
    }

    @ColorInt
    public int getBorderColor() {
        return colorBorder;
    }

    public void setBackgroundColor(Color color) {
        setBackgroundColor(color.toArgb());
    }

    public void setBackgroundColor(int color) {
        if (this.colorBackground == color) {
            return;
        }

        colorBackground = color;

        if (initialized) {
            invalidate();
            requestLayout();
        }
    }

    @ColorInt
    public int getBackgroundColor() {
        return colorBackground;
    }

    public void setForegroundColor(Color color) {
        setForegroundColor(color.toArgb());
    }

    public void setForegroundColor(int color) {
        if (this.colorForeground == color) {
            return;
        }

        colorForeground = color;

        if (initialized) {
            invalidate();
            requestLayout();
        }
    }

    @ColorInt
    public int getForegroundColor() {
        return colorForeground;
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

    public float getValue() {
        return value;
    }

    public void setOrientation(Orientation orientation) {
        if (this.orientation == orientation) {
            return;
        }

        this.orientation = orientation;

        switch (orientation) {
            case HORIZONTAL_LEFT:
                renderer = new HorizontalLeftRenderer();
                break;
            case HORIZONTAL_RIGHT:
                renderer = new HorizontalRightRenderer();
                break;
            case VERTICAL_TOP:
                renderer = new VerticalTopRenderer();
                break;
            case VERTICAL_BOTTOM:
                renderer = new VerticalBottomRenderer();
                break;

            default:
                throw new IllegalArgumentException("Invalid orientation value: " + orientation);
        }

        if (initialized) {
            invalidate();
            requestLayout();
        }
    }

    private void setOrientation(int orientation) {
        for (Orientation value : Orientation.values()) {
            if (value.ordinal() == orientation) {
                setOrientation(value);
                return;
            }
        }

        throw new IllegalArgumentException("Invalid orientation value: " + orientation);
    }

    public Orientation getOrientation() {
        return orientation;
    }
}
