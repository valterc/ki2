package com.valterc.ki2.views;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;

import androidx.annotation.Nullable;

import com.valterc.ki2.R;

public class DrivetrainView extends View {

    private static final int REAR_GEAR_INNER = 0;
    private static final int REAR_GEAR_MIDDLE = 1;
    private static final int REAR_GEAR_OUTER = 2;

    private static final int FRONT_GEAR_INNER = 0;
    private static final int FRONT_GEAR_OUTER = 1;

    private int frontGearMax;
    private int frontGear;
    private int rearGearMax;
    private int rearGear;

    private Paint drivetrainPaint;
    private Paint selectedGearPaint;
    private Paint chainPaint;

    private float rearGearPositionX;
    private float frontGearPositionX;
    private float rearGearPositionY;
    private float frontGearPositionY;
    private float rearGearRadius;
    private float frontGearRadius;

    private float rearTopDeraileurPositionX;
    private float rearTopDeraileurPositionY;
    private float rearBottomDeraileurPositionX;
    private float rearBottomDeraileurPositionY;
    private float rearDeraileurRadius;

    public DrivetrainView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        setFrontGearMax(2);
        setFrontGear(2);
        setRearGearMax(11);
        setRearGear(1);

        TypedArray array = context.getTheme().obtainStyledAttributes(attrs, R.styleable.DrivetrainView, 0, 0);

        try {

        } finally {
            array.recycle();
        }

        init();
    }

    private void init() {
        final Resources resources = getResources();

        drivetrainPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        drivetrainPaint.setStyle(Paint.Style.STROKE);
        drivetrainPaint.setStrokeWidth(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 2, resources.getDisplayMetrics()));
        drivetrainPaint.setColor(0xff1b2d2d);

        selectedGearPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        selectedGearPaint.setStyle(Paint.Style.STROKE);
        selectedGearPaint.setStrokeWidth(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 3, resources.getDisplayMetrics()));
        selectedGearPaint.setColor(0xffc84e35);

        chainPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        chainPaint.setStyle(Paint.Style.STROKE);
        chainPaint.setStrokeWidth(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 3, resources.getDisplayMetrics()));
        chainPaint.setColor(0xffdddddd);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        int internalWidth = w - (getPaddingStart() + getPaddingEnd());
        int internalHeight = h - (getPaddingTop() + getPaddingBottom());

        float horizontalCenter = (float) internalWidth * 0.5f;
        float verticalCenter = (float) internalHeight * 0.5f;

        rearGearPositionX = horizontalCenter * 0.5f;
        frontGearPositionX = horizontalCenter + horizontalCenter * 0.5f;

        rearGearPositionY = verticalCenter * 0.75f;
        frontGearPositionY = verticalCenter;

        rearGearRadius = Math.min(horizontalCenter * 0.55f, internalHeight * 0.45f) * 0.5f;
        frontGearRadius = Math.min(horizontalCenter * 0.7f, internalHeight * 0.7f) * 0.5f;

        rearDeraileurRadius = rearGearRadius * 0.25f;

        rearTopDeraileurPositionX = rearGearPositionX + rearGearRadius * 0.5f;
        rearTopDeraileurPositionY = rearGearPositionY + rearGearRadius + rearDeraileurRadius + drivetrainPaint.getStrokeWidth();

        rearBottomDeraileurPositionX = rearGearPositionX - rearGearRadius + ((float)rearGear / rearGearMax * (rearGearRadius * 2f));
        rearBottomDeraileurPositionY = rearTopDeraileurPositionY + rearDeraileurRadius * 3 + drivetrainPaint.getStrokeWidth() * 2;

    }

    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);

        drawFrontGears(canvas);
        drawRearGears(canvas);

        drawRearDeraileur(canvas);

        drawChain(canvas);
    }

    private void drawFrontGears(Canvas canvas) {
        float spacing = frontGearRadius * 0.66f / frontGearMax;

        for (int i = 1; i <= frontGearMax; i++) {
            canvas.drawCircle(frontGearPositionX, frontGearPositionY, frontGearRadius - (spacing * (frontGearMax - i)), drivetrainPaint);
        }
    }

    private void drawRearGears(Canvas canvas) {
        float spacing = rearGearRadius * 0.8f / rearGearMax;

        for (int i = 1; i <= rearGearMax; i++) {
            canvas.drawCircle(rearGearPositionX, rearGearPositionY, rearGearRadius - (spacing * (rearGearMax - i)), drivetrainPaint);
        }
    }

    private void drawRearDeraileur(Canvas canvas) {
        canvas.drawCircle(rearTopDeraileurPositionX, rearTopDeraileurPositionY, rearDeraileurRadius, selectedGearPaint);
        canvas.drawCircle(rearBottomDeraileurPositionX, rearBottomDeraileurPositionY, rearDeraileurRadius, selectedGearPaint);

    }

    private void drawChain(Canvas canvas) {
            float frontGearSpacing = frontGearRadius * 0.66f / frontGearMax;
            float rearGearSpacing = rearGearRadius * 0.8f / rearGearMax;

            canvas.drawLine(rearGearPositionX, rearGearPositionY - rearGearRadius + (rearGearSpacing * (rearGearMax - rearGear)), frontGearPositionX, frontGearPositionY - frontGearRadius + (frontGearSpacing * (frontGearMax - frontGear)), chainPaint);

            canvas.drawLine(rearGearPositionX, rearGearPositionY + rearGearRadius - (rearGearSpacing * (rearGearMax - rearGear)), rearTopDeraileurPositionX, rearTopDeraileurPositionY - rearDeraileurRadius, chainPaint);
            canvas.drawLine(rearTopDeraileurPositionX + rearDeraileurRadius, rearTopDeraileurPositionY, rearBottomDeraileurPositionX - rearDeraileurRadius, rearBottomDeraileurPositionY, chainPaint);

            canvas.drawLine(rearBottomDeraileurPositionX, rearBottomDeraileurPositionY + rearDeraileurRadius, frontGearPositionX, frontGearPositionY + frontGearRadius - (frontGearSpacing * (frontGearMax - frontGear)), chainPaint);

    }

    public int getFrontGearMax() {
        return frontGearMax;
    }

    public void setFrontGearMax(int frontGearMax) {
        this.frontGearMax = frontGearMax;
        invalidate();
        requestLayout();
    }

    public int getFrontGear() {
        return frontGear;
    }

    public void setFrontGear(int frontGear) {
        this.frontGear = frontGear;
        invalidate();
        requestLayout();
    }

    public int getRearGearMax() {
        return rearGearMax;
    }

    public void setRearGearMax(int rearGearMax) {
        this.rearGearMax = rearGearMax;
        invalidate();
        requestLayout();
    }

    public int getRearGear() {
        return rearGear;
    }

    public void setRearGear(int rearGear) {
        this.rearGear = rearGear;
        invalidate();
        requestLayout();
    }

}
