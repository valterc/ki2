package com.valterc.ki2.fragments.settings.overlay.position;

import android.content.Context;
import android.content.res.Resources;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.RelativeLayout;

import com.valterc.ki2.data.preferences.PreferencesView;

public class ScaledPositionManager {

    private int parentWidth;
    private int parentHeight;

    private float scaleX;
    private float scaleY;

    public ScaledPositionManager(View parent) {
        ((View) parent.getParent()).getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                ((View) parent.getParent()).getViewTreeObserver().removeOnGlobalLayoutListener(this);
                parentWidth = ((View) parent.getParent()).getWidth() - ((View) parent.getParent()).getPaddingStart() - ((View) parent.getParent()).getPaddingEnd();
                parentHeight = ((View) parent.getParent()).getHeight() - ((View) parent.getParent()).getPaddingTop() - ((View) parent.getParent()).getPaddingBottom();

                DisplayMetrics displayMetrics = Resources.getSystem().getDisplayMetrics();
                int screenWidth = displayMetrics.widthPixels;
                int screenHeight = displayMetrics.heightPixels;

                scaleX = (float) parentWidth / screenWidth;
                scaleY = (float) parentHeight / screenHeight;

                parent.setScaleX(scaleX);
                parent.setScaleY(scaleY);

                parentWidth = parent.getWidth();
                parentHeight = parent.getHeight();
            }
        });
    }

    public float getScaleX() {
        return scaleX;
    }

    public float getScaleY() {
        return scaleY;
    }

    public void applyPosition(Context context, PreferencesView preferencesView, View view) {
        final int x = preferencesView.getOverlayPositionX(context);
        final int y = preferencesView.getOverlayPositionY(context);
        applyPosition(x, y, view);
    }

    public void applyPosition(int x, int y, View view) {
        int viewWidth = view.getWidth();
        int viewHeight = view.getHeight();

        if ((viewWidth == 0 && viewHeight == 0) || parentHeight == 0 || parentWidth == 0) {
            view.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    view.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    applyPositionInternal(x, y, view);
                }
            });
        } else {
            applyPositionInternal(x, y, view);
        }
    }

    public void applyPositionCenter(float x, float y, View view) {
        float viewWidth = view.getWidth();
        float viewHeight = view.getHeight();

        if ((viewWidth == 0 && viewHeight == 0) || parentHeight == 0 || parentWidth == 0) {
            view.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    view.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    applyPositionInternalCenter(x, y, view);
                }
            });
        } else {
            applyPositionInternalCenter(x, y, view);
        }
    }

    private void applyPositionInternal(float x, float y, View view) {
        ViewGroup.LayoutParams layoutParams = view.getLayoutParams();

        float viewWidth = (view.getWidth() + view.getPaddingStart() + view.getPaddingEnd());
        float viewHeight = (view.getHeight() + view.getPaddingStart() + view.getPaddingBottom());

        if ((view.getWidth() == 0 && view.getHeight() == 0) || parentHeight == 0 || parentWidth == 0) {
            return;
        }

        float xValue = x;
        float yValue = y;

        if (xValue < 0) {
            xValue = 0;
        } else if (xValue + viewWidth > parentWidth) {
            xValue = parentWidth - viewWidth;
        }

        if (yValue < 0) {
            yValue = 0;
        } else if (yValue + viewHeight > parentHeight) {
            yValue = parentHeight - viewHeight;
        }

        RelativeLayout.LayoutParams layoutParamsRelativeLayout = (RelativeLayout.LayoutParams) layoutParams;
        layoutParamsRelativeLayout.leftMargin = (int) xValue;
        layoutParamsRelativeLayout.topMargin = (int) yValue;

        view.setLayoutParams(layoutParamsRelativeLayout);
    }

    private void applyPositionInternalCenter(float x, float y, View view) {
        int viewWidth = view.getWidth() + view.getPaddingStart() + view.getPaddingEnd();
        int viewHeight = view.getHeight() + view.getPaddingStart() + view.getPaddingBottom();

        if ((view.getWidth() == 0 && view.getHeight() == 0) || parentHeight == 0 || parentWidth == 0) {
            return;
        }

        float halfWidth = ((float) viewWidth) / 2;
        float halfHeight = ((float) viewHeight) / 2;

        applyPositionInternal((int) (x - halfWidth), (int) (y - halfHeight), view);
    }
}
