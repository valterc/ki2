package com.valterc.ki2.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Picture;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.valterc.ki2.R;

@SuppressWarnings({"UnnecessaryLocalVariable", "unused"})
public class SlimGearsView extends View {

    private static final int DEFAULT_FRONT_GEAR_MAX = 2;
    private static final int DEFAULT_REAR_GEAR_MAX = 11;

    private static final int DEFAULT_FRONT_GEAR = 1;
    private static final int DEFAULT_REAR_GEAR = 1;

    private final boolean initialized;
    private Paint gearPaint;
    private Paint selectedFrontGearPaint;
    private Paint selectedRearGearPaint;
    private Picture picture;

    private int frontGearMax;
    private int frontGear;
    private int rearGearMax;
    private int rearGear;

    public SlimGearsView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initPaint();

        try (TypedArray array = context.getTheme().obtainStyledAttributes(attrs, R.styleable.SlimGearsView, 0, 0)) {
            setFrontGearMax(array.getInt(R.styleable.SlimGearsView_frontGearMax, DEFAULT_FRONT_GEAR_MAX));
            setRearGearMax(array.getInt(R.styleable.SlimGearsView_rearGearMax, DEFAULT_REAR_GEAR_MAX));
            setFrontGear(array.getInt(R.styleable.SlimGearsView_frontGear, DEFAULT_FRONT_GEAR));
            setRearGear(array.getInt(R.styleable.SlimGearsView_rearGear, DEFAULT_REAR_GEAR));

            int selectedFrontGearColor = array.getColor(R.styleable.SlimGearsView_selectedFrontGearColor, -1);
            if (selectedFrontGearColor != -1) {
                setSelectedFrontGearColor(selectedFrontGearColor);
            } else {
                setSelectedFrontGearColor(context.getColor(R.color.red_faded));
            }

            int selectedRearGearColor = array.getColor(R.styleable.SlimGearsView_selectedRearGearColor, -1);
            if (selectedRearGearColor != -1) {
                setSelectedRearGearColor(selectedRearGearColor);
            } else {
                setSelectedRearGearColor(context.getColor(R.color.red_faded));
            }

            int gearColor = array.getColor(R.styleable.SlimGearsView_gearColor, -1);
            if (gearColor != -1) {
                setGearColor(gearColor);
            } else {
                setGearColor(context.getColor(R.color.hh_black_dark));
            }
        }

        setFocusable(false);
        initialized = true;
    }

    private void initPaint() {
        gearPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        gearPaint.setStyle(Paint.Style.FILL);

        selectedFrontGearPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        selectedFrontGearPaint.setStyle(Paint.Style.FILL);

        selectedRearGearPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        selectedRearGearPaint.setStyle(Paint.Style.FILL);
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

        int gearsWidth = internalWidth;
        int gearsHeight = internalHeight;

        float spaceBetweenGears = 3;
        float spaceBetweenSets = internalWidth * 0.05f;

        int totalGears = rearGearMax + frontGearMax;
        float gearWidth = (internalWidth - (spaceBetweenGears * totalGears) - spaceBetweenSets) / totalGears;

        float frontGearsPositionX = getPaddingStart();

        drawFrontGears(canvas,
                gearsHeight - gearPaint.getStrokeWidth() * 2,
                frontGearsPositionX,
                getPaddingTop() + gearPaint.getStrokeWidth(),
                gearWidth + spaceBetweenGears,
                spaceBetweenGears);

        float rearGearsPositionX = getPaddingStart() + (gearWidth + spaceBetweenGears * 2) * frontGearMax + spaceBetweenSets;

        drawRearGears(canvas,
                gearsHeight - gearPaint.getStrokeWidth() * 2,
                rearGearsPositionX,
                getPaddingTop() + gearPaint.getStrokeWidth(),
                gearWidth,
                spaceBetweenGears);

        picture.endRecording();
    }

    private void drawFrontGears(Canvas canvas, float availableHeight, float positionX, float positionY, float gearWidth, float spaceBetweenGears) {
        for (int i = 1; i <= frontGearMax; i++) {
            Paint paint = i == frontGear ? selectedFrontGearPaint : gearPaint;
            canvas.drawRect(positionX, positionY, positionX + gearWidth, availableHeight, paint);
            positionX += gearWidth + spaceBetweenGears;
        }
    }

    private void drawRearGears(Canvas canvas, float availableHeight, float positionX, float positionY, float gearWidth, float spaceBetweenGears) {
        for (int i = 1; i <= rearGearMax; i++) {
            Paint paint = i == rearGear ? selectedRearGearPaint : gearPaint;
            canvas.drawRect(positionX, positionY, positionX + gearWidth, availableHeight, paint);
            positionX += gearWidth + spaceBetweenGears;
        }
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        super.onDraw(canvas);

        if (picture != null) {
            canvas.drawPicture(picture);
        }
    }

    public void setGears(int frontGearMax, int frontGear, int rearGearMax, int rearGear) {
        if (frontGearMax <= 0) {
            throw new IllegalArgumentException("Invalid front gear max value:" + frontGearMax);
        }

        if (frontGear <= 0 || frontGear > frontGearMax) {
            throw new IllegalArgumentException("Invalid front gear value:" + frontGear);
        }

        if (rearGearMax <= 0) {
            throw new IllegalArgumentException("Invalid rear gear max value:" + rearGearMax);
        }

        if (rearGear <= 0 || rearGear > rearGearMax) {
            throw new IllegalArgumentException("Invalid rear gear value:" + rearGear);
        }

        if (this.frontGearMax != frontGearMax ||
                this.frontGear != frontGear ||
                this.rearGearMax != rearGearMax ||
                this.rearGear != rearGear) {
            this.frontGearMax = frontGearMax;
            this.frontGear = frontGear;
            this.rearGearMax = rearGearMax;
            this.rearGear = rearGear;

            if (initialized) {
                invalidate();
                requestLayout();
            }
        }
    }

    public void setGears(int frontGearMax, int frontGear, String frontGearLabel, int rearGearMax, int rearGear, String rearGearLabel) {
        if (frontGearMax <= 0) {
            throw new IllegalArgumentException("Invalid front gear max value:" + frontGearMax);
        }

        if (frontGear <= 0 || frontGear > frontGearMax) {
            throw new IllegalArgumentException("Invalid front gear value:" + frontGear);
        }

        if (rearGearMax <= 0) {
            throw new IllegalArgumentException("Invalid rear gear max value:" + rearGearMax);
        }

        if (rearGear <= 0 || rearGear > rearGearMax) {
            throw new IllegalArgumentException("Invalid rear gear value:" + rearGear);
        }

        if (this.frontGearMax != frontGearMax ||
                this.frontGear != frontGear ||
                this.rearGearMax != rearGearMax ||
                this.rearGear != rearGear) {
            this.frontGearMax = frontGearMax;
            this.frontGear = frontGear;
            this.rearGearMax = rearGearMax;
            this.rearGear = rearGear;

            if (initialized) {
                invalidate();
                requestLayout();
            }
        }
    }

    public int getFrontGearMax() {
        return frontGearMax;
    }

    public void setFrontGearMax(int frontGearMax) {
        if (frontGearMax <= 0) {
            throw new IllegalArgumentException("Invalid front gear max value:" + frontGearMax);
        }

        if (this.frontGearMax != frontGearMax) {
            this.frontGearMax = frontGearMax;

            if (initialized) {
                invalidate();
                requestLayout();
            }
        }
    }

    public int getFrontGear() {
        return frontGear;
    }

    public void setFrontGear(int frontGear) {
        if (frontGear <= 0 || frontGear > frontGearMax) {
            throw new IllegalArgumentException("Invalid front gear value:" + frontGear);
        }

        if (this.frontGear != frontGear) {
            this.frontGear = frontGear;

            if (initialized) {
                invalidate();
                requestLayout();
            }
        }
    }

    public int getRearGearMax() {
        return rearGearMax;
    }

    public void setRearGearMax(int rearGearMax) {
        if (rearGearMax <= 0) {
            throw new IllegalArgumentException("Invalid rear gear max value:" + rearGearMax);
        }

        if (this.rearGearMax != rearGearMax) {
            this.rearGearMax = rearGearMax;

            if (initialized) {
                invalidate();
                requestLayout();
            }
        }
    }

    public int getRearGear() {
        return rearGear;
    }

    public void setRearGear(int rearGear) {
        if (rearGear <= 0 || rearGear > rearGearMax) {
            throw new IllegalArgumentException("Invalid rear gear value:" + rearGear);
        }

        if (this.rearGear != rearGear) {
            this.rearGear = rearGear;

            if (initialized) {
                invalidate();
                requestLayout();
            }
        }
    }

    public int getSelectedRearGearColor() {
        return selectedRearGearPaint.getColor();
    }

    public void setSelectedRearGearColor(int selectedGearColor) {
        this.selectedRearGearPaint.setColor(selectedGearColor);

        if (initialized) {
            invalidate();
            requestLayout();
        }
    }

    public int getSelectedFrontGearColor() {
        return selectedFrontGearPaint.getColor();
    }

    public void setSelectedFrontGearColor(int selectedGearColor) {
        this.selectedFrontGearPaint.setColor(selectedGearColor);

        if (initialized) {
            invalidate();
            requestLayout();
        }
    }

    public int getGearColor() {
        return gearPaint.getColor();
    }

    public void setGearColor(int gearBorderColor) {
        this.gearPaint.setColor(gearBorderColor);

        if (initialized) {
            invalidate();
            requestLayout();
        }
    }

}
