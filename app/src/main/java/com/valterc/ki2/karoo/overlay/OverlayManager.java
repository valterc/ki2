package com.valterc.ki2.karoo.overlay;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.valterc.ki2.data.connection.ConnectionInfo;
import com.valterc.ki2.data.device.BatteryInfo;
import com.valterc.ki2.data.device.DeviceId;
import com.valterc.ki2.data.message.ShowOverlayMessage;
import com.valterc.ki2.data.preferences.PreferencesView;
import com.valterc.ki2.data.preferences.device.DevicePreferencesView;
import com.valterc.ki2.data.shifting.ShiftingInfo;
import com.valterc.ki2.karoo.Ki2Context;
import com.valterc.ki2.karoo.handlers.IRideHandler;
import com.valterc.ki2.karoo.overlay.view.IOverlayView;
import com.valterc.ki2.karoo.overlay.view.builder.OverlayViewBuilderEntry;
import com.valterc.ki2.karoo.overlay.view.builder.OverlayViewBuilderRegistry;
import com.valterc.ki2.utils.ActivityUtils;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

@SuppressLint("LogNotTimber")
public class OverlayManager implements IRideHandler {

    private final Ki2Context ki2Context;
    private final LayoutInflater layoutInflater;
    private final Handler handler;

    private int activityHashCode;
    private IOverlayView view;

    private OverlayTriggers overlayTriggers;
    private long timestampLastTrigger;
    private boolean overlayEnabled;
    private int overlayDuration;

    private PreferencesView preferences;
    private ConnectionInfo connectionInfo;
    private ShiftingInfo shiftingInfo;
    private BatteryInfo batteryInfo;
    private DevicePreferencesView devicePreferences;

    @SuppressWarnings("FieldCanBeLocal")
    private final BiConsumer<DeviceId, ConnectionInfo> connectionInfoListener = (deviceId, connectionInfo) -> {
        this.connectionInfo = connectionInfo;
        overlayTriggers.onConnectionInfo(connectionInfo);
        showOverlay(false);
    };

    @SuppressWarnings("FieldCanBeLocal")
    private final BiConsumer<DeviceId, BatteryInfo> batteryInfoListener = (deviceId, batteryInfo) -> {
        this.batteryInfo = batteryInfo;
        overlayTriggers.onBatteryInfo(batteryInfo);
        showOverlay(false);
    };

    @SuppressWarnings("FieldCanBeLocal")
    private final BiConsumer<DeviceId, ShiftingInfo> shiftingInfoListener = (deviceId, shiftingInfo) -> {
        this.shiftingInfo = shiftingInfo;
        overlayTriggers.onShiftingInfo(shiftingInfo);
        showOverlay(false);
    };

    @SuppressWarnings("FieldCanBeLocal")
    private final BiConsumer<DeviceId, DevicePreferencesView> devicePreferencesListener = (deviceId, devicePreferences) -> {
        this.devicePreferences = devicePreferences;
        showOverlay(false);
    };

    @SuppressWarnings("FieldCanBeLocal")
    private final Consumer<PreferencesView> preferencesListener = (preferences) -> {
        this.preferences = preferences;
        updatePreferences();
        removeView();
    };

    @SuppressWarnings("FieldCanBeLocal")
    private final Consumer<ShowOverlayMessage> showOverlayListener = (showOverlayMessage) -> showOverlay(true);

    public OverlayManager(Ki2Context ki2Context) {
        this.ki2Context = ki2Context;
        this.handler = ki2Context.getHandler();
        this.layoutInflater = LayoutInflater.from(ki2Context.getSdkContext()).cloneInContext(ki2Context.getSdkContext());

        ki2Context.getServiceClient().registerPreferencesWeakListener(preferencesListener);
        ki2Context.getServiceClient().registerShiftingInfoWeakListener(shiftingInfoListener);
        ki2Context.getServiceClient().registerDevicePreferencesWeakListener(devicePreferencesListener);
        ki2Context.getServiceClient().registerBatteryInfoWeakListener(batteryInfoListener);
        ki2Context.getServiceClient().registerConnectionInfoWeakListener(connectionInfoListener);
        ki2Context.getServiceClient().getCustomMessageClient().registerShowOverlayWeakListener(showOverlayListener);
    }

    private void updatePreferences() {
        overlayEnabled = preferences.isOverlayEnabled(ki2Context.getSdkContext());
        overlayDuration = preferences.getOverlayDuration(ki2Context.getSdkContext());
        overlayTriggers = new OverlayTriggers(preferences.getOverlayTriggers(ki2Context.getSdkContext()));
    }

    private boolean ensureView() {
        if (!overlayEnabled){
            return false;
        }

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
            return false;
        }

        ViewGroup viewGroupBase = (ViewGroup) viewBase;
        OverlayViewBuilderEntry viewBuilder = OverlayViewBuilderRegistry.getBuilder(preferences.getOverlayTheme(ki2Context.getSdkContext()));

        if (viewBuilder != null) {
            View viewOverlay = layoutInflater.inflate(viewBuilder.getLayoutId(), viewGroupBase, false);
            viewGroupBase.addView(viewOverlay);
            view = viewBuilder.createOverlayView(ki2Context, viewOverlay);
            view.hide();
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

    private void showOverlay(boolean force) {
        if (!ensureView()) {
            return;
        }

        if (preferences == null || connectionInfo == null || devicePreferences == null ||
                (!force && !overlayTriggers.queryAndClearShouldShowOverlay())) {
            return;
        }

        timestampLastTrigger = System.currentTimeMillis();
        view.updateView(preferences, connectionInfo, devicePreferences, batteryInfo, shiftingInfo);
        view.show();

        handler.postDelayed(() -> {
            if (System.currentTimeMillis() - timestampLastTrigger >= overlayDuration) {
                if (view != null) {
                    view.hide();
                }
            }
        }, overlayDuration);
    }

}
