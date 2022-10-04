package com.valterc.ki2.views;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;

import androidx.annotation.Nullable;

import com.valterc.ki2.R;

public class DrivetrainView extends View {

    private int frontGearMax;
    private int frontGear;
    private int rearGearMax;
    private int rearGear;

    private Paint drivetrainPaint;
    private Paint chainPaint;

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

    private float rearTopDeraileurPositionX;
    private float rearTopDeraileurPositionY;
    private float rearBottomDeraileurPositionX;
    private float rearBottomDeraileurPositionY;
    private float rearDeraileurRadius;

    private Path chainPath;


    public DrivetrainView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        setFrontGearMax(2);
        setFrontGear(2);
        setRearGearMax(11);
        setRearGear(11);

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

        Paint selectedGearPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        selectedGearPaint.setStyle(Paint.Style.STROKE);
        selectedGearPaint.setStrokeWidth(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 3, resources.getDisplayMetrics()));
        selectedGearPaint.setColor(0xffc84e35);

        chainPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        chainPaint.setStyle(Paint.Style.STROKE);
        chainPaint.setStrokeWidth(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 3, resources.getDisplayMetrics()));
        chainPaint.setColor(0xffdddddd);
        chainPaint.setStrokeMiter(1);
        chainPaint.setStrokeJoin(Paint.Join.ROUND);
        chainPaint.setStrokeCap(Paint.Cap.ROUND);
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

        frontGearSpacing = frontGearRadius * 0.66f / frontGearMax;
        rearGearSpacing = rearGearRadius * 0.8f / rearGearMax;

        rearDeraileurRadius = rearGearRadius * 0.25f;

        rearTopDeraileurPositionX = rearGearPositionX + rearGearRadius * 0.5f;
        rearTopDeraileurPositionY = rearGearPositionY + rearGearRadius + rearDeraileurRadius + drivetrainPaint.getStrokeWidth();

        rearBottomDeraileurPositionX = rearGearPositionX - rearGearRadius + ((float) rearGear / rearGearMax * (rearGearRadius * 2f));
        rearBottomDeraileurPositionY = rearTopDeraileurPositionY + rearDeraileurRadius * 3 + drivetrainPaint.getStrokeWidth() * 2;

        currentRearGearRadius = rearGearRadius - (rearGearSpacing * (rearGearMax - rearGear));
        currentFrontGearRadius = frontGearRadius - (frontGearSpacing * (frontGearMax - frontGear));

        initChainPath();
    }

    private void initChainPath(){

        float chainTopOriginPositionX = rearGearPositionX;
        float chainTopOriginPositionY = rearGearPositionY - currentRearGearRadius;

        float chainTopTargetPositionX = frontGearPositionX;
        float chainTopTargetPositionY = frontGearPositionY - currentFrontGearRadius;

        float chainBetweenGearAndDeraileurOriginPositionX = rearGearPositionX;
        float chainBetweenGearAndDeraileurOriginPositionY = rearGearPositionY + currentRearGearRadius;

        double deltaX = Math.abs(rearTopDeraileurPositionX - chainBetweenGearAndDeraileurOriginPositionX);
        double deltaY = Math.abs(rearTopDeraileurPositionY - chainBetweenGearAndDeraileurOriginPositionY);
        double thetaRadians = Math.atan2(deltaY, deltaX);

        float chainBetweenGearAndDeraileurTargetPositionX = rearTopDeraileurPositionX + (float) (rearDeraileurRadius * Math.sin(thetaRadians));
        float chainBetweenGearAndDeraileurTargetPositionY = rearTopDeraileurPositionY - (float) (rearDeraileurRadius * Math.cos(thetaRadians));

        deltaX = Math.abs(rearTopDeraileurPositionX - rearBottomDeraileurPositionX);
        deltaY = Math.abs(rearTopDeraileurPositionY - rearBottomDeraileurPositionY);
        thetaRadians = Math.atan2(deltaY, deltaX);

        float chainBetweenDeraileurWheelsOriginPositionX = rearTopDeraileurPositionX + (float) (rearDeraileurRadius * Math.sin(thetaRadians));
        float chainBetweenDeraileurWheelsOriginPositionY = rearTopDeraileurPositionY + (float) (rearDeraileurRadius * Math.cos(thetaRadians));

        deltaX = Math.abs(rearBottomDeraileurPositionX - rearTopDeraileurPositionX);
        deltaY = Math.abs(rearBottomDeraileurPositionY - rearTopDeraileurPositionY);
        thetaRadians = Math.atan2(deltaY, deltaX);

        float chainBetweenDeraileurWheelsTargetPositionX = rearBottomDeraileurPositionX - (float) (rearDeraileurRadius * Math.sin(thetaRadians));
        float chainBetweenDeraileurWheelsTargetPositionY = rearBottomDeraileurPositionY - (float) (rearDeraileurRadius * Math.cos(thetaRadians));

        float chainBottomOriginPositionX = rearBottomDeraileurPositionX;
        float chainBottomOriginPositionY = rearBottomDeraileurPositionY + rearDeraileurRadius;
        float chainBottomTargetPositionX = frontGearPositionX;
        float chainBottomTargetPositionY = frontGearPositionY + frontGearRadius - (frontGearSpacing * (frontGearMax - frontGear));

        chainPath = new Path();

        chainPath.moveTo(chainTopOriginPositionX, chainTopOriginPositionY);
        chainPath.lineTo(chainTopTargetPositionX, chainTopTargetPositionY);

        float startAngle = (float)Math.toDegrees(Math.atan2(chainTopOriginPositionY - rearGearPositionY, chainTopOriginPositionX - rearGearPositionX));
        float sweepAngle = (float)Math.toDegrees(Math.atan2(chainTopOriginPositionY - chainBetweenGearAndDeraileurOriginPositionY, chainTopOriginPositionX - chainBetweenGearAndDeraileurOriginPositionX));
        chainPath.addArc(
                rearGearPositionX - currentRearGearRadius,
                rearGearPositionY - currentRearGearRadius,
                rearGearPositionX + currentRearGearRadius,
                rearGearPositionY + currentRearGearRadius,
                startAngle, sweepAngle * 2f);

        chainPath.moveTo(chainBetweenGearAndDeraileurOriginPositionX, chainBetweenGearAndDeraileurOriginPositionY);
        chainPath.lineTo(chainBetweenGearAndDeraileurTargetPositionX, chainBetweenGearAndDeraileurTargetPositionY);

        startAngle = (float)Math.toDegrees(Math.atan2(chainBetweenGearAndDeraileurTargetPositionY - rearTopDeraileurPositionY, chainBetweenGearAndDeraileurTargetPositionX - rearTopDeraileurPositionX));
        sweepAngle = (float)Math.toDegrees(Math.atan2(chainBetweenDeraileurWheelsOriginPositionY - chainBetweenGearAndDeraileurTargetPositionY, chainBetweenDeraileurWheelsOriginPositionX - chainBetweenGearAndDeraileurTargetPositionX));
        chainPath.addArc(
                rearTopDeraileurPositionX - rearDeraileurRadius,
                rearTopDeraileurPositionY - rearDeraileurRadius,
                rearTopDeraileurPositionX + rearDeraileurRadius,
                rearTopDeraileurPositionY + rearDeraileurRadius,
                startAngle, sweepAngle);

        chainPath.moveTo(chainBetweenDeraileurWheelsOriginPositionX, chainBetweenDeraileurWheelsOriginPositionY);
        chainPath.lineTo(chainBetweenDeraileurWheelsTargetPositionX, chainBetweenDeraileurWheelsTargetPositionY);


        startAngle = (float)Math.toDegrees(Math.atan2(chainBetweenDeraileurWheelsTargetPositionY - rearBottomDeraileurPositionY, chainBetweenDeraileurWheelsTargetPositionX - rearBottomDeraileurPositionX));
        sweepAngle = (float)Math.toDegrees(Math.atan2(chainBottomOriginPositionY - chainBetweenDeraileurWheelsTargetPositionY, chainBottomOriginPositionX - chainBetweenDeraileurWheelsTargetPositionX));
        chainPath.addArc(
                rearBottomDeraileurPositionX - rearDeraileurRadius,
                rearBottomDeraileurPositionY - rearDeraileurRadius,
                rearBottomDeraileurPositionX + rearDeraileurRadius,
                rearBottomDeraileurPositionY + rearDeraileurRadius,
                startAngle, -sweepAngle * 2f);

        chainPath.moveTo(chainBottomOriginPositionX, chainBottomOriginPositionY);
        chainPath.lineTo(chainBottomTargetPositionX, chainBottomTargetPositionY);

        startAngle = (float)Math.toDegrees(Math.atan2(chainBottomTargetPositionY - frontGearPositionY, chainBottomTargetPositionX - frontGearPositionX));
        sweepAngle = (float)Math.toDegrees(Math.atan2(chainBottomTargetPositionY - chainTopTargetPositionY, chainBottomTargetPositionX - chainTopTargetPositionX));
        chainPath.addArc(
                frontGearPositionX - currentFrontGearRadius,
                frontGearPositionY - currentFrontGearRadius,
                frontGearPositionX + currentFrontGearRadius,
                frontGearPositionY + currentFrontGearRadius,
                startAngle, -sweepAngle * 2f);
    }

    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);

        drawFrontGears(canvas);
        drawRearGears(canvas);
        drawRearDeraileur(canvas);
        drawChainPath(canvas);
    }

    private void drawFrontGears(Canvas canvas) {
        for (int i = 1; i <= frontGearMax; i++) {
            canvas.drawCircle(frontGearPositionX, frontGearPositionY, frontGearRadius - (frontGearSpacing * (frontGearMax - i)), drivetrainPaint);
        }
    }

    private void drawRearGears(Canvas canvas) {
        for (int i = 1; i <= rearGearMax; i++) {
            canvas.drawCircle(rearGearPositionX, rearGearPositionY, rearGearRadius - (rearGearSpacing * (rearGearMax - i)), drivetrainPaint);
        }
    }

    private void drawRearDeraileur(Canvas canvas) {
        canvas.drawCircle(rearTopDeraileurPositionX, rearTopDeraileurPositionY, rearDeraileurRadius, drivetrainPaint);
        canvas.drawCircle(rearBottomDeraileurPositionX, rearBottomDeraileurPositionY, rearDeraileurRadius, drivetrainPaint);
    }

    private void drawChainPath(Canvas canvas) {
        canvas.drawPath(chainPath, chainPaint);
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
