package com.valterc.ki2.karoo.overlay;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.valterc.ki2.data.device.BatteryInfo;
import com.valterc.ki2.data.device.DeviceId;
import com.valterc.ki2.data.preferences.PreferencesView;
import com.valterc.ki2.data.preferences.device.DevicePreferencesView;
import com.valterc.ki2.data.shifting.ShiftingInfo;
import com.valterc.ki2.karoo.Ki2Context;
import com.valterc.ki2.karoo.overlay.view.IOverlayView;
import com.valterc.ki2.karoo.overlay.view.builder.ViewBuilderEntry;
import com.valterc.ki2.karoo.overlay.view.builder.ViewBuilderRegistry;
import com.valterc.ki2.utils.ActivityUtils;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

@SuppressLint("LogNotTimber")
public class OverlayManager {

    private static final int TIME_MS_AUTO_HIDE = 2000;

    @SuppressWarnings("FieldCanBeLocal")
    private final BiConsumer<DeviceId, BatteryInfo> batteryInfoListener = (deviceId, batteryInfo) -> {
        this.batteryInfo = batteryInfo;
        updateView();
    };

    @SuppressWarnings("FieldCanBeLocal")
    private final BiConsumer<DeviceId, ShiftingInfo> shiftingInfoListener = (deviceId, shiftingInfo) -> {
        this.shiftingInfo = shiftingInfo;
        updateView();
    };

    @SuppressWarnings("FieldCanBeLocal")
    private final BiConsumer<DeviceId, DevicePreferencesView> devicePreferencesListener = (deviceId, devicePreferences) -> {
        this.devicePreferences = devicePreferences;
        updateView();
    };

    @SuppressWarnings("FieldCanBeLocal")
    private final Consumer<PreferencesView> preferencesListener = (preferences) -> {
        this.preferences = preferences;
        removeView();
    };

    private final LayoutInflater layoutInflater;
    private final Handler handler;

    private int activityHashCode;
    private IOverlayView view;

    private long timestampLastDataUpdate;
    private PreferencesView preferences;
    private ShiftingInfo shiftingInfo;
    private BatteryInfo batteryInfo;
    private DevicePreferencesView devicePreferences;

    public OverlayManager(Ki2Context ki2Context) {
        this.handler = ki2Context.getHandler();
        this.layoutInflater = LayoutInflater.from(ki2Context.getSdkContext()).cloneInContext(ki2Context.getSdkContext());

        ki2Context.getServiceClient().registerShiftingInfoWeakListener(shiftingInfoListener);
        ki2Context.getServiceClient().registerBatteryInfoWeakListener(batteryInfoListener);
        ki2Context.getServiceClient().registerDevicePreferencesWeakListener(devicePreferencesListener);
        ki2Context.getServiceClient().registerPreferencesWeakListener(preferencesListener);
    }

    private boolean ensureView() {
        Activity activity = ActivityUtils.getRunningActivity();
        if (activity == null) {
            removeView();
            return false;
        }

        if (view != null && activityHashCode == activity.hashCode()) {
            return true;
        }

        removeView();

        ViewGroup viewGroupContent = activity.findViewById(android.R.id.content);
        if (viewGroupContent == null || viewGroupContent.getChildCount() == 0) {
            Log.w("KI2", "Unable to get ride activity root view");
            return false;
        }

        View viewBase = viewGroupContent.getChildAt(0);
        if (!(viewBase instanceof ViewGroup)) {
            Log.w("KI2", "Ride activity root view is not a ViewGroup");
            view = null;
            return false;
        }

        ViewGroup viewGroupBase = (ViewGroup) viewBase;
        ViewBuilderEntry viewBuilder = ViewBuilderRegistry.getBuilder("light");

        if (viewBuilder != null) {
            View viewOverlay = layoutInflater.inflate(viewBuilder.getLayoutId(), viewGroupBase, false);
            viewGroupBase.addView(viewOverlay);
            view = viewBuilder.createOverlayView(layoutInflater.getContext(), viewOverlay);
        }

        activityHashCode = activity.hashCode();
        return true;
    }

    private void removeView() {
        if (view != null) {
            view.remove();
            view = null;
        }
    }

    private void updateView() {
        if (!ensureView()) {
            return;
        }

        if (shiftingInfo == null || batteryInfo == null || devicePreferences == null) {
            return;
        }

        timestampLastDataUpdate = System.currentTimeMillis();
        view.updateView(shiftingInfo, batteryInfo, devicePreferences);
        view.show();

        handler.postDelayed(() -> {
            if (System.currentTimeMillis() - timestampLastDataUpdate >= TIME_MS_AUTO_HIDE) {
                if (view != null) {
                    view.hide();
                }
            }
        }, TIME_MS_AUTO_HIDE);
    }

}
