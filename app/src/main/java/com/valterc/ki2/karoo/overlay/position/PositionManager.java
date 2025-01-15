package com.valterc.ki2.karoo.overlay.position;

import android.content.res.Resources;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.RelativeLayout;

public class PositionManager {

    private final WindowManager windowManager;
    private final View overlayView;
    private final View parentView;

    private final int x;
    private final int y;

    private final int parentWidth;
    private final int parentHeight;

    private int lastX;
    private int lastY;

    private int lastWidth = -1;
    private int lastHeight = -1;

    public PositionManager(int positionX, int positionY, WindowManager windowManager, View overlayView, View parentView) {
        DisplayMetrics displayMetrics = Resources.getSystem().getDisplayMetrics();
        parentWidth = displayMetrics.widthPixels;
        parentHeight = displayMetrics.heightPixels;

        this.x = positionX;
        this.y = positionY;

        this.windowManager = windowManager;
        this.overlayView = overlayView;
        this.parentView = parentView;
    }

    public void updatePosition() {
        if (overlayView == null || parentHeight == 0 || parentWidth == 0) {
            return;
        }

        overlayView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);

        int viewWidth = overlayView.getMeasuredWidth() + overlayView.getPaddingStart() + overlayView.getPaddingEnd();
        int viewHeight = overlayView.getMeasuredHeight() + overlayView.getPaddingTop() + overlayView.getPaddingBottom();

        if (overlayView.getMeasuredWidth() == 0 || overlayView.getMeasuredHeight() == 0) {
            viewWidth = overlayView.getWidth() + overlayView.getPaddingStart() + overlayView.getPaddingEnd();
            viewHeight = overlayView.getHeight() + overlayView.getPaddingTop() + overlayView.getPaddingBottom();
        }

        if (lastWidth == viewWidth && lastHeight == viewHeight) {
            return;
        }

        lastWidth = viewWidth;
        lastHeight = viewHeight;

        int xValue = x;
        int yValue = y;

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

        if (lastX == xValue && lastY == yValue) {
            return;
        }

        RelativeLayout.LayoutParams layoutParamsOverlay = (RelativeLayout.LayoutParams) overlayView.getLayoutParams();
        WindowManager.LayoutParams layoutParamsParent = (WindowManager.LayoutParams) parentView.getLayoutParams();

        if (layoutParamsOverlay.width != ViewGroup.LayoutParams.MATCH_PARENT) {
            layoutParamsParent.x = xValue;
            layoutParamsParent.gravity |= Gravity.START;
        }

        if (layoutParamsOverlay.height != ViewGroup.LayoutParams.MATCH_PARENT) {
            layoutParamsParent.y = yValue;
            layoutParamsParent.gravity |= Gravity.TOP;
        }

        lastX = xValue;
        lastY = yValue;
        windowManager.updateViewLayout(parentView, layoutParamsParent);
    }
}
