package com.valterc.ki2.views;

import android.animation.TimeAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Picture;
import android.graphics.Rect;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

import com.valterc.ki2.R;

public class ChangeShiftModeButtonView extends View {

    private static final int TIME_MS_DONE_COOLDOWN = 1000;

    private TimeAnimator timeAnimator;
    private Picture picture;

    private TextPaint paintText;
    private Paint paintStateNormal;
    private Paint paintStateConfirming;
    private Paint paintStateDone;

    private boolean confirming;
    private boolean done;
    private long doneTimestamp;
    private float confirmationProgress;

    private String text;
    private String[] textLines;

    private Runnable runnable;

    public ChangeShiftModeButtonView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initPaint();
        setText("Change\nMode");

        setClickable(true);
        setOnClickListener(this::onClickListener);

        if (!isInEditMode()) {
            timeAnimator = new TimeAnimator();
            timeAnimator.setTimeListener(this::onTimeUpdate);
        }
    }

    private void initPaint() {
        paintText = new TextPaint(Paint.ANTI_ALIAS_FLAG);
        paintText.setTypeface(Typeface.DEFAULT);
        paintText.setTextAlign(Paint.Align.CENTER);
        paintText.setColor(Color.WHITE);

        paintStateNormal = new Paint(Paint.ANTI_ALIAS_FLAG);
        paintStateNormal.setStyle(Paint.Style.FILL);
        paintStateNormal.setShader(new LinearGradient(0, getPaddingTop(), 0, getHeight() - getPaddingBottom(),
                getContext().getColor(R.color.hh_gears_active_red_top),
                getContext().getColor(R.color.hh_gears_active_red_bottom),
                Shader.TileMode.CLAMP));

        paintStateConfirming = new Paint(Paint.ANTI_ALIAS_FLAG);
        paintStateConfirming.setStyle(Paint.Style.FILL);
        paintStateConfirming.setShader(new LinearGradient(0, getPaddingTop(), 0, getHeight() - getPaddingBottom(),
                getContext().getColor(R.color.hh_light_blue),
                getContext().getColor(R.color.hh_dark_blue),
                Shader.TileMode.CLAMP));

        paintStateDone = new Paint(Paint.ANTI_ALIAS_FLAG);
        paintStateDone.setStyle(Paint.Style.FILL);
        paintStateDone.setShader(new LinearGradient(0, getPaddingTop(), 0, getHeight() - getPaddingBottom(),
                getContext().getColor(R.color.hh_dark_blue),
                getContext().getColor(R.color.hh_gears_active_red_bottom),
                Shader.TileMode.CLAMP));
    }

    private void onClickListener(View view) {
        if (!timeAnimator.isRunning()) {
            confirmationProgress = 0;
            confirming = true;
            setText("Press\nAgain");
            timeAnimator.start();
        } else if (timeAnimator.isRunning() && !done) {
            confirming = false;
            done = true;
            setText("Done");
            doneTimestamp = System.currentTimeMillis();
            if (runnable != null) {
                runnable.run();
            }
        }
    }

    private void onTimeUpdate(TimeAnimator timeAnimator, long totalTime, long deltaTime) {
        if (confirming) {
            if (confirmationProgress >= .99) {
                confirming = false;
                setText("Change\nMode");
                timeAnimator.cancel();
            } else {
                confirmationProgress = Math.min(confirmationProgress + 0.0005f * deltaTime, 1);
            }
        } else if (done) {
            if (System.currentTimeMillis() - doneTimestamp > TIME_MS_DONE_COOLDOWN) {
                timeAnimator.cancel();
                setText("Change\nMode");
                done = false;
            }
        }

        drawPicture();
        postInvalidateOnAnimation();
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        initPaint();
        drawPicture();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (picture != null) {
            canvas.drawPicture(picture);
        }
    }

    private void drawPicture() {
        picture = new Picture();
        Canvas canvas = picture.beginRecording(getWidth(), getHeight());

        canvas.drawRoundRect(getPaddingStart(), getPaddingTop(), getWidth() - getPaddingEnd(), getHeight() - getPaddingBottom(), 10, 10, paintStateNormal);

        if (confirming) {
            canvas.save();
            canvas.clipRect(getPaddingStart(), getPaddingTop(), getWidth() - getPaddingEnd(), (getHeight() - getPaddingBottom()) * confirmationProgress);
            canvas.drawRoundRect(getPaddingStart(), getPaddingTop(), getWidth() - getPaddingEnd(), getHeight() - getPaddingBottom(), 10, 10, paintStateConfirming);
            canvas.restore();
        } else if (done) {
            canvas.drawRoundRect(getPaddingStart(), getPaddingTop(), getWidth() - getPaddingEnd(), getHeight() - getPaddingBottom(), 10, 10, paintStateDone);
        }

        if (text != null) {
            adjustTextSize();

            float lineDistance = paintText.descent() - paintText.ascent();

            float x = (float) getWidth() / 2;
            float y = (float) getHeight() / 2 - (textLines.length * lineDistance) / 2 - paintText.ascent();

            for (String line : textLines) {
                canvas.drawText(line, x, y, paintText);
                y += lineDistance;
            }
        }

        picture.endRecording();
    }

    private int textSizeInternalWidth;
    private int textSizeInternalHeight;

    private void adjustTextSize() {
        int internalWidth = getWidth() - (getPaddingStart() + getPaddingEnd());
        int internalHeight = getHeight() - (getPaddingTop() + getPaddingBottom());

        if (internalWidth == textSizeInternalWidth && internalHeight == textSizeInternalHeight) {
            return;
        }

        paintText.setTextSize(1);

        while (true) {
            Rect textBounds = new Rect();
            paintText.getTextBounds("CHANGE", 0, "CHANGE".length(), textBounds);

            float lineDistance = paintText.descent() - paintText.ascent();
            if (paintText.getTextSize() >= 49 || textBounds.width() >= internalWidth || lineDistance * 2 >= internalHeight) {
                paintText.setTextSize(paintText.getTextSize() - 1);
                textSizeInternalWidth = internalWidth;
                textSizeInternalHeight = internalHeight;
                return;
            }

            paintText.setTextSize(paintText.getTextSize() + 1);
        }
    }

    private void setText(String text) {
        if (text == null) {
            this.text = null;
            this.textLines = new String[0];
        } else if (this.text == null || !this.text.equals(text)) {
            this.text = text;
            this.textLines = text.split("\n");
        }
    }

    public void setOnActionListener(Runnable runnable) {
        this.runnable = runnable;
    }

}
