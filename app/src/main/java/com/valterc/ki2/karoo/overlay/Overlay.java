package com.valterc.ki2.karoo.overlay;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.valterc.ki2.R;
import com.valterc.ki2.data.device.DeviceId;
import com.valterc.ki2.data.shifting.ShiftingInfo;
import com.valterc.ki2.karoo.Ki2Context;
import com.valterc.ki2.utils.ActivityUtils;

import java.util.function.BiConsumer;

@SuppressLint("LogNotTimber")
public class Overlay {

    @SuppressWarnings("FieldCanBeLocal")
    private final BiConsumer<DeviceId, ShiftingInfo> shiftingInfoListener = (deviceId, shiftingInfo) -> {
        ensureView();
    };

    private final Ki2Context ki2Context;
    private final LayoutInflater layoutInflater;

    private int activityHashCode;
    private View viewOverlay;

    public Overlay(Ki2Context ki2Context) {
        this.ki2Context = ki2Context;
        this.layoutInflater = LayoutInflater.from(this.ki2Context.getSdkContext()).cloneInContext(this.ki2Context.getSdkContext());
        ki2Context.getServiceClient().registerShiftingInfoWeakListener(shiftingInfoListener);
    }

    private boolean ensureView() {
        Activity activity = ActivityUtils.getRunningActivity();
        if (activity == null) {
            return false;
        }

        if (activityHashCode == activity.hashCode()) {
            return true;
        }

        ViewGroup viewGroupContent = activity.findViewById(android.R.id.content);
        if (viewGroupContent == null || viewGroupContent.getChildCount() == 0) {
            Log.w("KI2", "Unable to get ride activity root view");
            return false;
        }

        View viewBase = viewGroupContent.getChildAt(0);
        if (!(viewBase instanceof ViewGroup)) {
            Log.w("KI2", "Ride activity root view is not a ViewGroup");
            return false;
        }

        ViewGroup viewGroupBase = (ViewGroup) viewBase;
        viewOverlay = layoutInflater.inflate(R.layout.view_karoo_overlay, viewGroupBase, false);
        viewGroupBase.addView(viewOverlay);

        activityHashCode = activity.hashCode();
        return true;
    }


}
