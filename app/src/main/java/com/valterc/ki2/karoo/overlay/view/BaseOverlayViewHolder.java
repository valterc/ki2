package com.valterc.ki2.karoo.overlay.view;

import android.annotation.SuppressLint;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;

import androidx.annotation.NonNull;

@SuppressLint("LogNotTimber")
public class BaseOverlayViewHolder {

    private final View overlayView;

    public BaseOverlayViewHolder(@NonNull View overlayView) {
        this.overlayView = overlayView;
    }

    public void removeFromHierarchy() {
        try {
            ViewParent parent = this.overlayView.getParent();
            if (parent != null) {
                ((ViewGroup) parent).removeView(overlayView);
            }
        } catch (Exception e) {
            Log.w("KI2", "Unable to remove overlay from parent");
        }
    }

    public View getOverlayView() {
        return overlayView;
    }

}
