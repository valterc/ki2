package com.valterc.ki2.views;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Picture;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.valterc.ki2.R;

import java.util.Objects;

@SuppressWarnings({"UnnecessaryLocalVariable", "unused"})
public class GearsView extends View {

    private static final int DEFAULT_FRONT_GEAR_MAX = 2;
    private static final int DEFAULT_REAR_GEAR_MAX = 11;

    private static final int DEFAULT_FRONT_GEAR = 1;
    private static final int DEFAULT_REAR_GEAR = 1;

    private static final boolean DEFAULT_TEXT_ENABLED = true;

    private static final String STRING_MEASURE = "F1/";
    private static final String STRING_REAR_PREFIX = "";
    private static final String STRING_FRONT_PREFIX = "";
    private static final String STRING_GEAR_SEPARATOR = "/";

    private final boolean initialized;
    private final Path textPath;
    private final Path tempPath1;
    private final Path tempPath2;
    private final Rect tempRect;
    private Paint unselectedGearPaint;
    private Paint selectedGearPaint;
    private Picture picture;
    private boolean textEnabled;
    private Paint textPaint;
    private int frontGearMax;
    private int frontGear;
    private int rearGearMax;
    private int rearGear;
    private String frontGearLabel;
    private String rearGearLabel;

    public GearsView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initPaint();

        try (TypedArray array = context.getTheme().obtainStyledAttributes(attrs, R.styleable.GearsView, 0, 0)) {
            setFrontGearMax(array.getInt(R.styleable.GearsView_frontGearMax, DEFAULT_FRONT_GEAR_MAX));
            setRearGearMax(array.getInt(R.styleable.GearsView_rearGearMax, DEFAULT_REAR_GEAR_MAX));
            setFrontGear(array.getInt(R.styleable.GearsView_frontGear, DEFAULT_FRONT_GEAR));
            setRearGear(array.getInt(R.styleable.GearsView_rearGear, DEFAULT_REAR_GEAR));

            setFrontGearLabel(array.getString(R.styleable.GearsView_frontGearLabel));
            setRearGearLabel(array.getString(R.styleable.GearsView_rearGearLabel));

            setTextEnabled(array.getBoolean(R.styleable.GearsView_textEnabled, DEFAULT_TEXT_ENABLED));

            int textSize = array.getDimensionPixelSize(R.styleable.GearsView_android_textSize, -1);
            if (textSize != -1) {
                textPaint.setTextSize(textSize);
            } else {
                TypedValue sizeValue = new TypedValue();
                context.getTheme().resolveAttribute(android.R.attr.textSize, sizeValue, true);
                setTextSize(sizeValue.data);
            }

            int textColor = array.getColor(R.styleable.GearsView_android_textColor, -1);
            if (textColor != -1) {
                setTextColor(textColor);
            } else {
                TypedValue colorValue = new TypedValue();
                context.getTheme().resolveAttribute(android.R.attr.textColorPrimary, colorValue, true);
                setTextColor(colorValue.data);
            }

            int selectedGearColor = array.getColor(R.styleable.GearsView_selectedGearColor, -1);
            if (selectedGearColor != -1) {
                setSelectedGearColor(selectedGearColor);
            } else {
                setSelectedGearColor(context.getColor(R.color.hh_gears_active_dark));
            }

            int unselectedGearBorderColor = array.getColor(R.styleable.GearsView_unselectedGearBorderColor, -1);
            if (unselectedGearBorderColor != -1) {
                setUnselectedGearBorderColor(unselectedGearBorderColor);
            } else {
                setUnselectedGearBorderColor(context.getColor(R.color.hh_gears_border_dark));
            }
        }

        setFocusable(false);

        textPath = new Path();
        tempPath1 = new Path();
        tempPath2 = new Path();
        tempRect = new Rect();

        initialized = true;
    }

    private void initPaint() {
        selectedGearPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        selectedGearPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        selectedGearPaint.setStrokeWidth(3f);

        unselectedGearPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        unselectedGearPaint.setStyle(Paint.Style.STROKE);
        unselectedGearPaint.setStrokeWidth(3f);

        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        drawPicture();
    }

    private int measureTextHeight(float padding) {
        textPaint.setTypeface(Typeface.DEFAULT_BOLD);
        textPaint.getTextBounds(STRING_MEASURE, 0, STRING_MEASURE.length(), tempRect);
        return (int) (tempRect.height() + padding + 0.5);
    }

    private void drawPicture() {
        picture = new Picture();
        Canvas canvas = picture.beginRecording(getWidth(), getHeight());

        int internalWidth = getWidth() - (getPaddingStart() + getPaddingEnd());
        int internalHeight = getHeight() - (getPaddingTop() + getPaddingBottom());

        int gearsWidth = internalWidth;
        int gearsHeight = internalHeight;

        float textPositionY = 0;

        if (textEnabled) {
            final Resources resources = getResources();
            float textVerticalPadding = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 3, resources.getDisplayMetrics());
            gearsHeight -= measureTextHeight(textVerticalPadding);
            textPositionY = getPaddingTop() + gearsHeight + textVerticalPadding;
        }

        float spaceBetweenGears = internalWidth * 0.02f;
        float spaceBetweenSets = internalWidth * 0.1f;

        int totalGears = rearGearMax + frontGearMax;
        float gearWidth = (gearsWidth - (spaceBetweenGears * (totalGears + frontGearMax)) - spaceBetweenSets) / totalGears;

        float frontGearsPositionX = getPaddingStart() + spaceBetweenGears;

        drawFrontGears(canvas,
                gearsHeight - unselectedGearPaint.getStrokeWidth() * 2,
                frontGearsPositionX,
                getPaddingTop() + unselectedGearPaint.getStrokeWidth(),
                gearWidth,
                spaceBetweenGears);

        float rearGearsPositionX = getPaddingStart() + (gearWidth + spaceBetweenGears * 2) * frontGearMax + spaceBetweenSets;

        drawRearGears(canvas,
                gearsHeight - unselectedGearPaint.getStrokeWidth() * 2,
                rearGearsPositionX,
                getPaddingTop() + unselectedGearPaint.getStrokeWidth(),
                gearWidth,
                spaceBetweenGears);

        if (textEnabled) {
            float frontGearsCenterPositionX = frontGearsPositionX + (frontGearMax * (gearWidth + spaceBetweenGears * 2) * .5f);

            drawGearsText(canvas,
                    frontGearsCenterPositionX,
                    textPositionY,
                    STRING_FRONT_PREFIX,
                    frontGearMax,
                    frontGear,
                    frontGearLabel);

            float rearGearsCenterPositionX = rearGearsPositionX + (rearGearMax * (gearWidth + spaceBetweenGears) * .5f);

            drawGearsText(canvas,
                    rearGearsCenterPositionX,
                    textPositionY,
                    STRING_REAR_PREFIX,
                    rearGearMax,
                    rearGear,
                    rearGearLabel);
        }

        picture.endRecording();
    }

    private void drawFrontGears(Canvas canvas, float availableHeight, float positionX, float positionY, float gearWidth, float spaceBetweenGears) {
        float minGearHeight = 0.7f;
        float gearBumpSize = frontGearMax == 1 ? 0 : ((1 - minGearHeight) * availableHeight) / (frontGearMax - 1);

        for (int i = 1; i <= frontGearMax; i++) {
            float positionYStart = positionY + (gearBumpSize * (frontGearMax - i));
            float positionYEnd = positionY + availableHeight;
            Paint paint = i == frontGear ? selectedGearPaint : unselectedGearPaint;
            canvas.drawRoundRect(positionX, positionYStart, positionX + gearWidth, positionYEnd, gearWidth * .5f, gearWidth * .5f, paint);
            positionX += gearWidth + spaceBetweenGears;
        }
    }

    private void drawRearGears(Canvas canvas, float availableHeight, float positionX, float positionY, float gearWidth, float spaceBetweenGears) {
        float minGearHeight = 0.6f;
        float gearBumpSize = rearGearMax == 1 ? 0 : (availableHeight * minGearHeight) / (rearGearMax - 1);

        for (int i = 1; i <= rearGearMax; i++) {
            float positionYStart = positionY + (gearBumpSize * (i - 1));
            float positionYEnd = positionY + availableHeight;
            Paint paint = i == rearGear ? selectedGearPaint : unselectedGearPaint;
            canvas.drawRoundRect(positionX, positionYStart, positionX + gearWidth, positionYEnd, gearWidth * .5f, gearWidth * .5f, paint);
            positionX += gearWidth + spaceBetweenGears;
        }
    }

    private void drawGearsText(Canvas canvas, float positionX, float positionY, String prefix, int gearsMax, int selectedGear, String label) {
        textPath.reset();
        tempPath1.reset();
        tempPath2.reset();

        textPaint.setTypeface(Typeface.DEFAULT_BOLD);
        textPaint.getTextBounds(STRING_MEASURE, 0, STRING_MEASURE.length(), tempRect);

        float appendedWidth;
        if (label != null) {
            appendedWidth = appendString(tempPath2, tempPath1, label, Typeface.DEFAULT, 0);
        } else {
            appendedWidth = appendString(tempPath2, tempPath1, prefix, Typeface.DEFAULT, 0);
            appendedWidth += appendString(tempPath2, tempPath1, Integer.toString(selectedGear), Typeface.DEFAULT_BOLD, appendedWidth);
            appendedWidth += appendString(tempPath2, tempPath1, STRING_GEAR_SEPARATOR, Typeface.DEFAULT, appendedWidth);
            appendedWidth += appendString(tempPath2, tempPath1, Integer.toString(gearsMax), Typeface.DEFAULT, appendedWidth);
        }

        textPath.addPath(tempPath2, Math.max(0, positionX - appendedWidth * .5f), positionY + tempRect.height());
        tempPath1.reset();
        tempPath2.reset();

        canvas.drawPath(textPath, textPaint);
        textPath.reset();
    }

    private float appendString(Path targetPath, Path scratchPath, String text, Typeface typeface, float x) {
        textPaint.setTypeface(typeface);
        textPaint.setTextAlign(Paint.Align.LEFT);
        textPaint.getTextPath(text, 0, text.length(), 0, 0, scratchPath);
        targetPath.addPath(scratchPath, x, 0);
        return textPaint.measureText(text);
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
            throw new IllegalArgumentException("Invalid front gear max value: " + frontGearMax);
        }

        if (frontGear <= 0 || frontGear > frontGearMax) {
            throw new IllegalArgumentException("Invalid front gear value: " + frontGear);
        }

        if (rearGearMax <= 0) {
            throw new IllegalArgumentException("Invalid rear gear max value: " + rearGearMax);
        }

        if (rearGear <= 0 || rearGear > rearGearMax) {
            throw new IllegalArgumentException("Invalid rear gear value: " + rearGear);
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
            throw new IllegalArgumentException("Invalid front gear max value: " + frontGearMax);
        }

        if (frontGear <= 0 || frontGear > frontGearMax) {
            throw new IllegalArgumentException("Invalid front gear value: " + frontGear);
        }

        if (rearGearMax <= 0) {
            throw new IllegalArgumentException("Invalid rear gear max value: " + rearGearMax);
        }

        if (rearGear <= 0 || rearGear > rearGearMax) {
            throw new IllegalArgumentException("Invalid rear gear value: " + rearGear);
        }

        if (this.frontGearMax != frontGearMax ||
                this.frontGear != frontGear ||
                this.rearGearMax != rearGearMax ||
                this.rearGear != rearGear ||
                !Objects.equals(this.frontGearLabel, frontGearLabel) ||
                !Objects.equals(this.rearGearLabel, rearGearLabel)) {
            this.frontGearMax = frontGearMax;
            this.frontGear = frontGear;
            this.frontGearLabel = frontGearLabel;
            this.rearGearMax = rearGearMax;
            this.rearGear = rearGear;
            this.rearGearLabel = rearGearLabel;

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
            throw new IllegalArgumentException("Invalid front gear max value: " + frontGearMax);
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
            throw new IllegalArgumentException("Invalid front gear value: " + frontGear);
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
            throw new IllegalArgumentException("Invalid rear gear max value: " + rearGearMax);
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
            throw new IllegalArgumentException("Invalid rear gear value: " + rearGear);
        }

        if (this.rearGear != rearGear) {
            this.rearGear = rearGear;

            if (initialized) {
                invalidate();
                requestLayout();
            }
        }
    }

    public String getFrontGearLabel() {
        return frontGearLabel;
    }

    public void setFrontGearLabel(String frontGearLabel) {
        if (!Objects.equals(this.frontGearLabel, frontGearLabel)) {
            this.frontGearLabel = frontGearLabel;

            if (initialized) {
                invalidate();
                requestLayout();
            }
        }
    }

    public String getRearGearLabel() {
        return rearGearLabel;
    }

    public void setRearGearLabel(String rearGearLabel) {
        if (!Objects.equals(this.rearGearLabel, rearGearLabel)) {
            this.rearGearLabel = rearGearLabel;

            if (initialized) {
                invalidate();
                requestLayout();
            }
        }
    }

    public float getTextSize() {
        return textPaint.getTextSize();
    }

    public void setTextSize(float textSize) {
        if (textSize < 0) {
            throw new IllegalArgumentException("Invalid text size: " + textSize);
        }

        final Resources resources = getResources();
        textPaint.setTextSize(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, textSize, resources.getDisplayMetrics()));

        if (initialized) {
            invalidate();
            requestLayout();
        }
    }

    public int getTextColor() {
        return textPaint.getColor();
    }

    public void setTextColor(Color textColor) {
        setTextColor(textColor.toArgb());
    }

    public void setTextColor(int textColor) {
        if (textPaint.getColor() == textColor) {
            return;
        }

        textPaint.setColor(textColor);

        if (initialized) {
            invalidate();
            requestLayout();
        }
    }

    public boolean isTextEnabled() {
        return textEnabled;
    }

    public void setTextEnabled(boolean textEnabled) {
        if (this.textEnabled == textEnabled) {
            return;
        }

        this.textEnabled = textEnabled;

        if (initialized) {
            invalidate();
            requestLayout();
        }
    }

    public int getSelectedGearColor() {
        return selectedGearPaint.getColor();
    }

    public void setSelectedGearColor(int selectedGearColor) {
        if (selectedGearPaint.getColor() == selectedGearColor) {
            return;
        }

        selectedGearPaint.setColor(selectedGearColor);

        if (initialized) {
            invalidate();
            requestLayout();
        }
    }

    public int getUnselectedGearBorderColor() {
        return unselectedGearPaint.getColor();
    }

    public void setUnselectedGearBorderColor(int unselectedGearBorderColor) {
        if (unselectedGearPaint.getColor() == unselectedGearBorderColor) {
            return;
        }
        unselectedGearPaint.setColor(unselectedGearBorderColor);

        if (initialized) {
            invalidate();
            requestLayout();
        }
    }
}
