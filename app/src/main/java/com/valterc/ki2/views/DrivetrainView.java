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
    private Paint selectedGearPaint;
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

    private float chainTopOriginPositionX;
    private float chainTopOriginPositionY;
    private float chainTopTargetPositionX;
    private float chainTopTargetPositionY;
    private float chainBetweenDeraileurWheelsOriginPositionX;
    private float chainBetweenDeraileurWheelsOriginPositionY;
    private float chainBetweenDeraileurWheelsTargetPositionX;
    private float chainBetweenDeraileurWheelsTargetPositionY;
    private float chainBetweenGearAndDeraileurOriginPositionX;
    private float chainBetweenGearAndDeraileurOriginPositionY;
    private float chainBetweenGearAndDeraileurTargetPositionX;
    private float chainBetweenGearAndDeraileurTargetPositionY;
    private float chainBottomOriginPositionX;
    private float chainBottomOriginPositionY;
    private float chainBottomTargetPositionX;
    private float chainBottomTargetPositionY;


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

        selectedGearPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        selectedGearPaint.setStyle(Paint.Style.STROKE);
        selectedGearPaint.setStrokeWidth(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 3, resources.getDisplayMetrics()));
        selectedGearPaint.setColor(0xffc84e35);

        chainPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        chainPaint.setStyle(Paint.Style.STROKE);
        chainPaint.setStrokeWidth(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 3, resources.getDisplayMetrics()));
        chainPaint.setColor(0xffdddddd);
        chainPaint.setStrokeMiter(10);
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

        chainTopOriginPositionX = rearGearPositionX;
        chainTopOriginPositionY = rearGearPositionY - currentRearGearRadius;

        chainTopTargetPositionX = frontGearPositionX;
        chainTopTargetPositionY = frontGearPositionY - currentFrontGearRadius;

        chainBetweenGearAndDeraileurOriginPositionX = rearGearPositionX;
        chainBetweenGearAndDeraileurOriginPositionY = rearGearPositionY + currentRearGearRadius;

        double deltaX = Math.abs(rearTopDeraileurPositionX - chainBetweenGearAndDeraileurOriginPositionX);
        double deltaY = Math.abs(rearTopDeraileurPositionY - chainBetweenGearAndDeraileurOriginPositionY);
        double thetaRadians = Math.atan2(deltaY, deltaX);

        chainBetweenGearAndDeraileurTargetPositionX = rearTopDeraileurPositionX + (float) (rearDeraileurRadius * Math.sin(thetaRadians));
        chainBetweenGearAndDeraileurTargetPositionY = rearTopDeraileurPositionY - (float) (rearDeraileurRadius * Math.cos(thetaRadians));

        deltaX = Math.abs(rearTopDeraileurPositionX - rearBottomDeraileurPositionX);
        deltaY = Math.abs(rearTopDeraileurPositionY - rearBottomDeraileurPositionY);
        thetaRadians = Math.atan2(deltaY, deltaX);

        chainBetweenDeraileurWheelsOriginPositionX = rearTopDeraileurPositionX + (float) (rearDeraileurRadius * Math.sin(thetaRadians));
        chainBetweenDeraileurWheelsOriginPositionY = rearTopDeraileurPositionY + (float) (rearDeraileurRadius * Math.cos(thetaRadians));

        deltaX = Math.abs(rearBottomDeraileurPositionX - rearTopDeraileurPositionX);
        deltaY = Math.abs(rearBottomDeraileurPositionY - rearTopDeraileurPositionY);
        thetaRadians = Math.atan2(deltaY, deltaX);

        chainBetweenDeraileurWheelsTargetPositionX = rearBottomDeraileurPositionX - (float) (rearDeraileurRadius * Math.sin(thetaRadians));
        chainBetweenDeraileurWheelsTargetPositionY = rearBottomDeraileurPositionY - (float) (rearDeraileurRadius * Math.cos(thetaRadians));

        chainBottomOriginPositionX = rearBottomDeraileurPositionX;
        chainBottomOriginPositionY = rearBottomDeraileurPositionY + rearDeraileurRadius;
        chainBottomTargetPositionX = frontGearPositionX;
        chainBottomTargetPositionY = frontGearPositionY + frontGearRadius - (frontGearSpacing * (frontGearMax - frontGear));

    }

    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);

        drawFrontGears(canvas);
        drawRearGears(canvas);

        drawRearDeraileur(canvas);

        //drawChain(canvas);
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

        Path path = new Path();

        path.moveTo(chainTopOriginPositionX, chainTopOriginPositionY);
        path.lineTo(chainTopTargetPositionX, chainTopTargetPositionY);

        int startAngle = (int) (180 / Math.PI * Math.atan2(chainTopOriginPositionY - rearGearPositionY, chainTopOriginPositionX - rearGearPositionX));
        float sweepAngle = (int) (180 / Math.PI * Math.atan2(chainTopOriginPositionY - chainBetweenGearAndDeraileurOriginPositionY, chainTopOriginPositionX - chainBetweenGearAndDeraileurOriginPositionX));
        path.addArc(
                rearGearPositionX - currentRearGearRadius,
                rearGearPositionY - currentRearGearRadius,
                rearGearPositionX + currentRearGearRadius,
                rearGearPositionY + currentRearGearRadius,
                startAngle, sweepAngle * 2f);

        path.moveTo(chainBetweenGearAndDeraileurOriginPositionX, chainBetweenGearAndDeraileurOriginPositionY);
        path.lineTo(chainBetweenGearAndDeraileurTargetPositionX, chainBetweenGearAndDeraileurTargetPositionY);

        startAngle = (int) (180 / Math.PI * Math.atan2(chainBetweenGearAndDeraileurTargetPositionY - rearTopDeraileurPositionY, chainBetweenGearAndDeraileurTargetPositionX - rearTopDeraileurPositionX));
        sweepAngle = (int) (180 / Math.PI * Math.atan2(chainBetweenDeraileurWheelsOriginPositionY - chainBetweenGearAndDeraileurTargetPositionY, chainBetweenDeraileurWheelsOriginPositionX - chainBetweenGearAndDeraileurTargetPositionX));
        path.addArc(
                rearTopDeraileurPositionX - rearDeraileurRadius,
                rearTopDeraileurPositionY - rearDeraileurRadius,
                rearTopDeraileurPositionX + rearDeraileurRadius,
                rearTopDeraileurPositionY + rearDeraileurRadius,
                startAngle, sweepAngle);

        path.moveTo(chainBetweenDeraileurWheelsOriginPositionX, chainBetweenDeraileurWheelsOriginPositionY);
        path.lineTo(chainBetweenDeraileurWheelsTargetPositionX, chainBetweenDeraileurWheelsTargetPositionY);


        startAngle = (int) (180 / Math.PI * Math.atan2(chainBetweenDeraileurWheelsTargetPositionY - rearBottomDeraileurPositionY, chainBetweenDeraileurWheelsTargetPositionX - rearBottomDeraileurPositionX));
        sweepAngle = (int) (180 / Math.PI * Math.atan2(chainBottomOriginPositionY - chainBetweenDeraileurWheelsTargetPositionY, chainBottomOriginPositionX - chainBetweenDeraileurWheelsTargetPositionX));
        path.addArc(
                rearBottomDeraileurPositionX - rearDeraileurRadius,
                rearBottomDeraileurPositionY - rearDeraileurRadius,
                rearBottomDeraileurPositionX + rearDeraileurRadius,
                rearBottomDeraileurPositionY + rearDeraileurRadius,
                startAngle, -sweepAngle * 2f);

        path.moveTo(chainBottomOriginPositionX, chainBottomOriginPositionY);
        path.lineTo(chainBottomTargetPositionX, chainBottomTargetPositionY);

        startAngle = (int) (180 / Math.PI * Math.atan2(chainBottomTargetPositionY - frontGearPositionY, chainBottomTargetPositionX - frontGearPositionX));
        sweepAngle = (int) (180 / Math.PI * Math.atan2(chainBottomTargetPositionY - chainTopTargetPositionY, chainBottomTargetPositionX - chainTopTargetPositionX));
        path.addArc(
                frontGearPositionX - currentFrontGearRadius,
                frontGearPositionY - currentFrontGearRadius,
                frontGearPositionX + currentFrontGearRadius,
                frontGearPositionY + currentFrontGearRadius,
                startAngle, -sweepAngle * 2f);

        canvas.drawPath(path, chainPaint);
    }

    private void drawChain(Canvas canvas) {
        canvas.drawLine(chainTopOriginPositionX - chainPaint.getStrokeWidth() * .5f, chainTopOriginPositionY, chainTopTargetPositionX + chainPaint.getStrokeWidth(), chainTopTargetPositionY, chainPaint);

        float startAngle = (float) Math.toDegrees(Math.atan2(chainTopOriginPositionY - rearGearPositionY, chainTopOriginPositionX - rearGearPositionX));
        float sweepAngle = (float) Math.toDegrees(Math.atan2(chainTopOriginPositionY - chainBetweenGearAndDeraileurOriginPositionY, chainTopOriginPositionX - chainBetweenGearAndDeraileurOriginPositionX));
        canvas.drawArc(
                rearGearPositionX - currentRearGearRadius,
                rearGearPositionY - currentRearGearRadius,
                rearGearPositionX + currentRearGearRadius,
                rearGearPositionY + currentRearGearRadius,
                startAngle, sweepAngle * 2, false, chainPaint);

        canvas.drawLine(chainBetweenGearAndDeraileurOriginPositionX, chainBetweenGearAndDeraileurOriginPositionY, chainBetweenGearAndDeraileurTargetPositionX, chainBetweenGearAndDeraileurTargetPositionY, chainPaint);

        startAngle = (float) Math.toDegrees(Math.atan2(chainBetweenGearAndDeraileurTargetPositionY - rearTopDeraileurPositionY, chainBetweenGearAndDeraileurTargetPositionX - rearTopDeraileurPositionX));
        sweepAngle = (float) Math.toDegrees(Math.atan2(chainBetweenGearAndDeraileurTargetPositionY - chainBetweenDeraileurWheelsOriginPositionY, chainBetweenGearAndDeraileurTargetPositionX - chainBetweenDeraileurWheelsOriginPositionX));
        canvas.drawArc(
                rearTopDeraileurPositionX - rearDeraileurRadius,
                rearTopDeraileurPositionY - rearDeraileurRadius,
                rearTopDeraileurPositionX + rearDeraileurRadius,
                rearTopDeraileurPositionY + rearDeraileurRadius,
                startAngle, -sweepAngle, false, chainPaint);

        canvas.drawLine(chainBetweenDeraileurWheelsOriginPositionX, chainBetweenDeraileurWheelsOriginPositionY, chainBetweenDeraileurWheelsTargetPositionX, chainBetweenDeraileurWheelsTargetPositionY, chainPaint);

        startAngle = (float) Math.toDegrees(Math.atan2(chainBetweenDeraileurWheelsTargetPositionY - rearBottomDeraileurPositionY, chainBetweenDeraileurWheelsTargetPositionX - rearBottomDeraileurPositionX));
        sweepAngle = (float) Math.toDegrees(Math.atan2(chainBottomOriginPositionY - chainBetweenDeraileurWheelsTargetPositionY, chainBottomOriginPositionX - chainBetweenDeraileurWheelsTargetPositionX));
        canvas.drawArc(
                rearBottomDeraileurPositionX - rearDeraileurRadius,
                rearBottomDeraileurPositionY - rearDeraileurRadius,
                rearBottomDeraileurPositionX + rearDeraileurRadius,
                rearBottomDeraileurPositionY + rearDeraileurRadius,
                startAngle, -sweepAngle * 2f, false, chainPaint);

        canvas.drawLine(chainBottomOriginPositionX - chainPaint.getStrokeWidth() * .5f, chainBottomOriginPositionY, chainBottomTargetPositionX + chainPaint.getStrokeWidth(), chainBottomTargetPositionY, chainPaint);

        startAngle = (float) Math.toDegrees(Math.atan2(chainBottomTargetPositionY - frontGearPositionY, chainBottomTargetPositionX - frontGearPositionX));
        sweepAngle = (float) Math.toDegrees(Math.atan2(chainBottomTargetPositionY - chainTopTargetPositionY, chainBottomTargetPositionX - chainTopTargetPositionX));
        canvas.drawArc(
                frontGearPositionX - currentFrontGearRadius,
                frontGearPositionY - currentFrontGearRadius,
                frontGearPositionX + currentFrontGearRadius,
                frontGearPositionY + currentFrontGearRadius,
                startAngle, -sweepAngle * 2f, false, chainPaint);

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
