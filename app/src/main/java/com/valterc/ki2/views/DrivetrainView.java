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

import androidx.annotation.Nullable;

import com.valterc.ki2.R;

import java.util.Objects;

@SuppressWarnings({"UnnecessaryLocalVariable", "unused"})
public class DrivetrainView extends View {

    private static final int DEFAULT_FRONT_GEAR_MAX = 2;
    private static final int DEFAULT_REAR_GEAR_MAX = 11;

    private static final int DEFAULT_FRONT_GEAR = 1;
    private static final int DEFAULT_REAR_GEAR = 1;

    private static final int DEFAULT_DRIVETRAIN_COLOR = 0xff1b2d2d;
    private static final int DEFAULT_SELECTED_GEAR_COLOR = 0xffc84e35;
    private static final int DEFAULT_CHAIN_COLOR = 0xffdddddd;

    private static final boolean DEFAULT_TEXT_ENABLED = true;

    private static final int DEFAULT_DRIVETRAIN_STROKE_WIDTH = 2;
    private static final int DEFAULT_SELECTED_GEAR_STROKE_WIDTH = 3;
    private static final int DEFAULT_CHAIN_STROKE_WIDTH = 3;

    private static final String STRING_MEASURE = "F1/";
    private static final String STRING_REAR_PREFIX = "";
    private static final String STRING_FRONT_PREFIX = "";
    private static final String STRING_GEAR_SEPARATOR = "/";

    private final boolean initialized;
    private Picture picture;

    private boolean textEnabled;

    private int frontGearMax;
    private int frontGear;
    private int rearGearMax;
    private int rearGear;

    private String frontGearLabel;
    private String rearGearLabel;

    private Paint drivetrainPaint;
    private Paint selectedGearPaint;
    private Paint chainPaint;
    private Paint textPaint;

    private float textPositionYOffset;

    private float rearGearPositionX;
    private float rearGearPositionY;

    private float frontGearPositionX;
    private float frontGearPositionY;

    private float rearGearRadius;
    private float rearGearSpacing;
    private float frontGearRadius;
    private float frontGearSpacing;

    private float currentRearGearRadius;
    private float currentFrontGearRadius;

    private float rearTopDerailleurPositionX;
    private float rearTopDerailleurPositionY;
    private float rearBottomDerailleurPositionX;
    private float rearBottomDerailleurPositionY;
    private float rearDerailleurRadius;

    private final Path chainPath;
    private final Path textPath;

    private final Path tempPath1;
    private final Path tempPath2;

    private final Rect tempRect;

    public DrivetrainView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initPaint();

        TypedArray array = context.getTheme().obtainStyledAttributes(attrs, R.styleable.DrivetrainView, 0, 0);
        try {
            setFrontGearMax(array.getInt(R.styleable.DrivetrainView_frontGearMax, DEFAULT_FRONT_GEAR_MAX));
            setRearGearMax(array.getInt(R.styleable.DrivetrainView_rearGearMax, DEFAULT_REAR_GEAR_MAX));
            setFrontGear(array.getInt(R.styleable.DrivetrainView_frontGear, DEFAULT_FRONT_GEAR));
            setRearGear(array.getInt(R.styleable.DrivetrainView_rearGear, DEFAULT_REAR_GEAR));

            setFrontGearLabel(array.getString(R.styleable.DrivetrainView_frontGearLabel));
            setRearGearLabel(array.getString(R.styleable.DrivetrainView_rearGearLabel));

            setDrivetrainColor(array.getColor(R.styleable.DrivetrainView_drivetrainColor, DEFAULT_DRIVETRAIN_COLOR));
            setSelectedGearColor(array.getColor(R.styleable.DrivetrainView_selectedGearColor, DEFAULT_SELECTED_GEAR_COLOR));
            setChainColor(array.getColor(R.styleable.DrivetrainView_chainColor, DEFAULT_CHAIN_COLOR));

            setTextEnabled(array.getBoolean(R.styleable.DrivetrainView_textEnabled, DEFAULT_TEXT_ENABLED));

            float drivetrainStrokeWidth = array.getDimension(R.styleable.DrivetrainView_drivetrainStrokeWidth, -1);
            if (drivetrainStrokeWidth != -1) {
                drivetrainPaint.setStrokeWidth(drivetrainStrokeWidth);
            } else {
                setDrivetrainStrokeWidth(DEFAULT_DRIVETRAIN_STROKE_WIDTH);
            }

            float selectedGearStrokeWidth = array.getDimension(R.styleable.DrivetrainView_selectedGearStrokeWidth, -1);
            if (selectedGearStrokeWidth != -1) {
                selectedGearPaint.setStrokeWidth(selectedGearStrokeWidth);
            } else {
                setSelectedGearStrokeWidth(DEFAULT_SELECTED_GEAR_STROKE_WIDTH);
            }

            float chainStrokeWidth = array.getDimension(R.styleable.DrivetrainView_chainStrokeWidth, -1);
            if (chainStrokeWidth != -1) {
                chainPaint.setStrokeWidth(chainStrokeWidth);
            } else {
                setChainStrokeWidth(DEFAULT_CHAIN_STROKE_WIDTH);
            }

            int textSize = array.getDimensionPixelSize(R.styleable.DrivetrainView_android_textSize, -1);
            if (textSize != -1) {
                textPaint.setTextSize(textSize);
            } else {
                TypedValue sizeValue = new TypedValue();
                context.getTheme().resolveAttribute(android.R.attr.textSize, sizeValue, true);
                setTextSize(sizeValue.data);
            }

            int textColor = array.getColor(R.styleable.DrivetrainView_android_textColor, -1);
            if (textColor != -1) {
                setTextColor(textColor);
            } else {
                TypedValue colorValue = new TypedValue();
                context.getTheme().resolveAttribute(android.R.attr.textColorPrimary, colorValue, true);
                setTextColor(colorValue.data);
            }
        } finally {
            array.recycle();
        }

        setFocusable(false);

        chainPath = new Path();
        textPath = new Path();
        tempPath1 = new Path();
        tempPath2 = new Path();
        tempRect = new Rect();

        initialized = true;
    }

    private void initPaint() {
        initDrivetrainPaint();
        initSelectedGearPaint();
        initChainPaint();
        initTextPaint();
    }

    private void initDrivetrainPaint() {
        drivetrainPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        drivetrainPaint.setStyle(Paint.Style.STROKE);
    }

    private void initSelectedGearPaint() {
        selectedGearPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        selectedGearPaint.setStyle(Paint.Style.STROKE);
    }

    private void initChainPaint() {
        chainPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        chainPaint.setStyle(Paint.Style.STROKE);
        chainPaint.setStrokeMiter(1);
        chainPaint.setStrokeJoin(Paint.Join.ROUND);
        chainPaint.setStrokeCap(Paint.Cap.ROUND);
    }

    private void initTextPaint() {
        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    }

    private int measureTextHeight(float padding) {
        textPaint.setTypeface(Typeface.DEFAULT_BOLD);
        textPaint.getTextBounds(STRING_MEASURE, 0, STRING_MEASURE.length(), tempRect);
        return (int) (tempRect.height() + padding + 0.5);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        computeMeasurements();
        drawPicture();
    }

    private void computeMeasurements() {
        int internalWidth = getWidth() - (getPaddingStart() + getPaddingEnd());
        int internalHeight = getHeight() - (getPaddingTop() + getPaddingBottom());

        int drivetrainBoxWidth = internalWidth;
        int drivetrainBoxHeight = internalHeight;

        final Resources resources = getResources();
        float textVerticalPadding = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 3, resources.getDisplayMetrics());

        if (textEnabled) {
            drivetrainBoxHeight -= measureTextHeight(textVerticalPadding);
            textPositionYOffset = drivetrainBoxHeight;
        }

        float horizontalCenter = (float) drivetrainBoxWidth * 0.5f;
        float verticalCenter = (float) drivetrainBoxHeight * 0.5f;

        rearGearPositionX = horizontalCenter * 0.3f;
        frontGearPositionX = horizontalCenter + horizontalCenter * 0.6f;

        rearGearRadius = Math.min(horizontalCenter * 0.5f, drivetrainBoxHeight * 0.4f) * 0.5f;
        frontGearRadius = Math.min(horizontalCenter * 0.75f, drivetrainBoxHeight * 0.75f) * 0.5f;

        frontGearPositionY = verticalCenter * 0.9f;
        rearGearPositionY = frontGearPositionY - frontGearRadius + rearGearRadius;

        frontGearSpacing = frontGearRadius * 0.66f / frontGearMax;
        rearGearSpacing = rearGearRadius * 0.8f / rearGearMax;

        rearDerailleurRadius = rearGearRadius * 0.25f;

        rearTopDerailleurPositionX = rearGearPositionX + rearGearRadius * 0.5f;
        rearTopDerailleurPositionY = rearGearPositionY + rearGearRadius + rearDerailleurRadius + drivetrainPaint.getStrokeWidth();

        rearBottomDerailleurPositionX = rearGearPositionX - rearGearRadius + ((1 - (float)(rearGear - 1) / rearGearMax) * (rearGearRadius * 2f));
        rearBottomDerailleurPositionY = rearTopDerailleurPositionY + rearDerailleurRadius * 3 + drivetrainPaint.getStrokeWidth() * 2;

        currentRearGearRadius = rearGearRadius - (rearGearSpacing * (rearGear - 1));
        currentFrontGearRadius = frontGearRadius - (frontGearSpacing * (frontGearMax - frontGear));

        initChainPath();
        if (textEnabled) {
            textPositionYOffset = Math.max(rearBottomDerailleurPositionY + rearDerailleurRadius, frontGearPositionY + frontGearRadius) + textVerticalPadding;
            initTextPath();
        }
    }

    private void initTextPath() {
        textPath.reset();
        tempPath1.reset();
        tempPath2.reset();

        textPaint.setTypeface(Typeface.DEFAULT_BOLD);
        textPaint.getTextBounds(STRING_MEASURE, 0, STRING_MEASURE.length(), tempRect);

        float appendedWidth;
        if (rearGearLabel != null) {
            appendedWidth = appendString(tempPath2, tempPath1, rearGearLabel, Typeface.DEFAULT, 0);
        } else {
            appendedWidth = appendString(tempPath2, tempPath1, STRING_REAR_PREFIX, Typeface.DEFAULT, 0);
            appendedWidth += appendString(tempPath2, tempPath1, Integer.toString(rearGear), Typeface.DEFAULT_BOLD, appendedWidth);
            appendedWidth += appendString(tempPath2, tempPath1, STRING_GEAR_SEPARATOR, Typeface.DEFAULT, appendedWidth);
            appendedWidth += appendString(tempPath2, tempPath1, Integer.toString(rearGearMax), Typeface.DEFAULT, appendedWidth);
        }

        textPath.addPath(tempPath2, Math.max(0, rearGearPositionX - appendedWidth * .5f), textPositionYOffset + tempRect.height());
        tempPath1.reset();
        tempPath2.reset();

        if (frontGearLabel != null) {
            appendedWidth = appendString(tempPath2, tempPath1, frontGearLabel, Typeface.DEFAULT, 0);
        } else {
            appendedWidth = appendString(tempPath2, tempPath1, STRING_FRONT_PREFIX, Typeface.DEFAULT, 0);
            appendedWidth += appendString(tempPath2, tempPath1, Integer.toString(frontGear), Typeface.DEFAULT_BOLD, appendedWidth);
            appendedWidth += appendString(tempPath2, tempPath1, STRING_GEAR_SEPARATOR, Typeface.DEFAULT, appendedWidth);
            appendedWidth += appendString(tempPath2, tempPath1, Integer.toString(frontGearMax), Typeface.DEFAULT, appendedWidth);
        }

        textPath.addPath(tempPath2, Math.min(frontGearPositionX - appendedWidth * .5f, getWidth() - appendedWidth), textPositionYOffset + tempRect.height());
    }

    private float appendString(Path targetPath, Path scratchPath, String text, Typeface typeface, float x) {
        textPaint.setTypeface(typeface);
        textPaint.setTextAlign(Paint.Align.LEFT);
        textPaint.getTextPath(text, 0, text.length(), 0, 0, scratchPath);
        targetPath.addPath(scratchPath, x, 0);
        return textPaint.measureText(text);
    }

    private void initChainPath() {
        float chainTopOriginPositionX = rearGearPositionX;
        float chainTopOriginPositionY = rearGearPositionY - currentRearGearRadius;

        float chainTopTargetPositionX = frontGearPositionX;
        float chainTopTargetPositionY = frontGearPositionY - currentFrontGearRadius;

        float chainBetweenGearAndDerailleurOriginPositionX = rearGearPositionX;
        float chainBetweenGearAndDerailleurOriginPositionY = rearGearPositionY + currentRearGearRadius;

        double deltaX = Math.abs(rearTopDerailleurPositionX - chainBetweenGearAndDerailleurOriginPositionX);
        double deltaY = Math.abs(rearTopDerailleurPositionY - chainBetweenGearAndDerailleurOriginPositionY);
        double thetaRadians = Math.atan2(deltaY, deltaX);

        float chainBetweenGearAndDerailleurTargetPositionX = rearTopDerailleurPositionX + (float) (rearDerailleurRadius * Math.sin(thetaRadians));
        float chainBetweenGearAndDerailleurTargetPositionY = rearTopDerailleurPositionY - (float) (rearDerailleurRadius * Math.cos(thetaRadians));

        deltaX = Math.abs(rearTopDerailleurPositionX - rearBottomDerailleurPositionX);
        deltaY = Math.abs(rearTopDerailleurPositionY - rearBottomDerailleurPositionY);
        thetaRadians = Math.atan2(deltaY, deltaX);

        float chainBetweenDerailleurWheelsOriginPositionX = rearTopDerailleurPositionX + (float) (rearDerailleurRadius * Math.sin(thetaRadians));
        float chainBetweenDerailleurWheelsOriginPositionY = rearTopDerailleurPositionY + (float) (rearDerailleurRadius * Math.cos(thetaRadians));

        deltaX = Math.abs(rearBottomDerailleurPositionX - rearTopDerailleurPositionX);
        deltaY = Math.abs(rearBottomDerailleurPositionY - rearTopDerailleurPositionY);
        thetaRadians = Math.atan2(deltaY, deltaX);

        float chainBetweenDerailleurWheelsTargetPositionX = rearBottomDerailleurPositionX - (float) (rearDerailleurRadius * Math.sin(thetaRadians));
        float chainBetweenDerailleurWheelsTargetPositionY = rearBottomDerailleurPositionY - (float) (rearDerailleurRadius * Math.cos(thetaRadians));

        float chainBottomOriginPositionX = rearBottomDerailleurPositionX;
        float chainBottomOriginPositionY = rearBottomDerailleurPositionY + rearDerailleurRadius;
        float chainBottomTargetPositionX = frontGearPositionX;
        float chainBottomTargetPositionY = frontGearPositionY + frontGearRadius - (frontGearSpacing * (frontGearMax - frontGear));

        chainPath.reset();

        chainPath.moveTo(chainTopOriginPositionX, chainTopOriginPositionY);
        chainPath.lineTo(chainTopTargetPositionX, chainTopTargetPositionY);

        float startAngle = (float) Math.toDegrees(Math.atan2(chainTopOriginPositionY - rearGearPositionY, chainTopOriginPositionX - rearGearPositionX));
        float sweepAngle = (float) Math.toDegrees(Math.atan2(chainTopOriginPositionY - chainBetweenGearAndDerailleurOriginPositionY, chainTopOriginPositionX - chainBetweenGearAndDerailleurOriginPositionX));
        chainPath.addArc(
                rearGearPositionX - currentRearGearRadius,
                rearGearPositionY - currentRearGearRadius,
                rearGearPositionX + currentRearGearRadius,
                rearGearPositionY + currentRearGearRadius,
                startAngle, sweepAngle * 2f);

        chainPath.moveTo(chainBetweenGearAndDerailleurOriginPositionX, chainBetweenGearAndDerailleurOriginPositionY);
        chainPath.lineTo(chainBetweenGearAndDerailleurTargetPositionX, chainBetweenGearAndDerailleurTargetPositionY);

        startAngle = (float) Math.toDegrees(Math.atan2(chainBetweenGearAndDerailleurTargetPositionY - rearTopDerailleurPositionY, chainBetweenGearAndDerailleurTargetPositionX - rearTopDerailleurPositionX));
        sweepAngle = (float) Math.toDegrees(Math.atan2(chainBetweenDerailleurWheelsOriginPositionY - chainBetweenGearAndDerailleurTargetPositionY, chainBetweenDerailleurWheelsOriginPositionX - chainBetweenGearAndDerailleurTargetPositionX));
        chainPath.addArc(
                rearTopDerailleurPositionX - rearDerailleurRadius,
                rearTopDerailleurPositionY - rearDerailleurRadius,
                rearTopDerailleurPositionX + rearDerailleurRadius,
                rearTopDerailleurPositionY + rearDerailleurRadius,
                startAngle, sweepAngle);

        chainPath.moveTo(chainBetweenDerailleurWheelsOriginPositionX, chainBetweenDerailleurWheelsOriginPositionY);
        chainPath.lineTo(chainBetweenDerailleurWheelsTargetPositionX, chainBetweenDerailleurWheelsTargetPositionY);


        startAngle = (float) Math.toDegrees(Math.atan2(chainBetweenDerailleurWheelsTargetPositionY - rearBottomDerailleurPositionY, chainBetweenDerailleurWheelsTargetPositionX - rearBottomDerailleurPositionX));
        sweepAngle = (float) Math.toDegrees(Math.atan2(chainBottomOriginPositionY - chainBetweenDerailleurWheelsTargetPositionY, chainBottomOriginPositionX - chainBetweenDerailleurWheelsTargetPositionX));
        chainPath.addArc(
                rearBottomDerailleurPositionX - rearDerailleurRadius,
                rearBottomDerailleurPositionY - rearDerailleurRadius,
                rearBottomDerailleurPositionX + rearDerailleurRadius,
                rearBottomDerailleurPositionY + rearDerailleurRadius,
                startAngle, -sweepAngle * 2f);

        chainPath.moveTo(chainBottomOriginPositionX, chainBottomOriginPositionY);
        chainPath.lineTo(chainBottomTargetPositionX, chainBottomTargetPositionY);

        startAngle = (float) Math.toDegrees(Math.atan2(chainBottomTargetPositionY - frontGearPositionY, chainBottomTargetPositionX - frontGearPositionX));
        sweepAngle = (float) Math.toDegrees(Math.atan2(chainBottomTargetPositionY - chainTopTargetPositionY, chainBottomTargetPositionX - chainTopTargetPositionX));
        chainPath.addArc(
                frontGearPositionX - currentFrontGearRadius,
                frontGearPositionY - currentFrontGearRadius,
                frontGearPositionX + currentFrontGearRadius,
                frontGearPositionY + currentFrontGearRadius,
                startAngle, -sweepAngle * 2f);
    }

    private void drawPicture(){
        picture = new Picture();
        Canvas canvas = picture.beginRecording(getWidth(), getHeight());

        drawFrontGears(canvas);
        drawRearGears(canvas);
        drawRearDerailleur(canvas);
        drawChainPath(canvas);
        drawText(canvas);

        picture.endRecording();
    }

    private void drawText(Canvas canvas) {
        if (textEnabled) {
            canvas.drawPath(textPath, textPaint);
        }
    }

    private void drawFrontGears(Canvas canvas) {
        for (int i = 1; i <= frontGearMax; i++) {
            canvas.drawCircle(frontGearPositionX, frontGearPositionY, frontGearRadius - (frontGearSpacing * (frontGearMax - i)), drivetrainPaint);
        }

        canvas.drawCircle(frontGearPositionX, frontGearPositionY, frontGearRadius - (frontGearSpacing * (frontGearMax - frontGear)), selectedGearPaint);
    }

    private void drawRearGears(Canvas canvas) {
        for (int i = 1; i <= rearGearMax; i++) {
            canvas.drawCircle(rearGearPositionX, rearGearPositionY, rearGearRadius - (rearGearSpacing * (i - 1)), drivetrainPaint);
        }

        canvas.drawCircle(rearGearPositionX, rearGearPositionY, rearGearRadius - (rearGearSpacing * (rearGear - 1)), selectedGearPaint);
    }

    private void drawRearDerailleur(Canvas canvas) {
        canvas.drawCircle(rearTopDerailleurPositionX, rearTopDerailleurPositionY, rearDerailleurRadius, drivetrainPaint);
        canvas.drawCircle(rearBottomDerailleurPositionX, rearBottomDerailleurPositionY, rearDerailleurRadius, drivetrainPaint);
    }

    private void drawChainPath(Canvas canvas) {
        canvas.drawPath(chainPath, chainPaint);
    }

    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);

        if (picture != null)
        {
            canvas.drawPicture(picture);
        }
    }

    public int getDrivetrainColor() {
        return drivetrainPaint.getColor();
    }

    public void setDrivetrainColor(int drivetrainColor) {
        drivetrainPaint.setColor(drivetrainColor);

        if (initialized) {
            invalidate();
            requestLayout();
        }
    }

    public void setDrivetrainColor(Color drivetrainColor) {
        setDrivetrainColor(drivetrainColor.toArgb());
    }

    public int getSelectedGearColor() {
        return selectedGearPaint.getColor();
    }

    public void setSelectedGearColor(int selectedGearColor) {
        selectedGearPaint.setColor(selectedGearColor);

        if (initialized) {
            invalidate();
            requestLayout();
        }
    }

    public void setSelectedGearColor(Color selectedGearColor) {
        setSelectedGearColor(selectedGearColor.toArgb());
    }

    public int getChainColor() {
        return chainPaint.getColor();
    }

    public void setChainColor(int chainColor) {
        chainPaint.setColor(chainColor);

        if (initialized) {
            invalidate();
            requestLayout();
        }
    }

    public void setChainColor(Color chainColor) {
        setChainColor(chainColor.toArgb());
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
            throw new IllegalArgumentException("Invalid front gear max value:" + frontGearMax);
        }

        if (this.frontGearMax !=  frontGearMax) {
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
            throw new IllegalArgumentException("Invalid text size:" + textSize);
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
        this.textEnabled = textEnabled;

        if (initialized) {
            invalidate();
            requestLayout();
        }
    }

    public float getDrivetrainStrokeWidth() {
        return drivetrainPaint.getStrokeWidth();
    }

    public void setDrivetrainStrokeWidth(float width) {
        if (width < 0) {
            throw new IllegalArgumentException("Invalid width:" + width);
        }

        final Resources resources = getResources();
        drivetrainPaint.setStrokeWidth(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, width, resources.getDisplayMetrics()));

        if (initialized) {
            invalidate();
            requestLayout();
        }
    }

    public float getSelectedGearStrokeWidth() {
        return selectedGearPaint.getStrokeWidth();
    }

    public void setSelectedGearStrokeWidth(float width) {
        if (width < 0) {
            throw new IllegalArgumentException("Invalid width:" + width);
        }

        final Resources resources = getResources();
        selectedGearPaint.setStrokeWidth(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, width, resources.getDisplayMetrics()));

        if (initialized) {
            invalidate();
            requestLayout();
        }
    }

    public float getChainStrokeWidth() {
        return chainPaint.getStrokeWidth();
    }

    public void setChainStrokeWidth(float width) {
        if (width < 0) {
            throw new IllegalArgumentException("Invalid width:" + width);
        }

        final Resources resources = getResources();
        chainPaint.setStrokeWidth(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, width, resources.getDisplayMetrics()));

        if (initialized) {
            invalidate();
            requestLayout();
        }
    }
}
