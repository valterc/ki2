package com.valterc.ki2.karoo.overlay.manager;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.RelativeLayout;

import com.valterc.ki2.R;
import com.valterc.ki2.data.connection.ConnectionInfo;
import com.valterc.ki2.data.device.BatteryInfo;
import com.valterc.ki2.data.device.DeviceId;
import com.valterc.ki2.data.preferences.PreferencesView;
import com.valterc.ki2.data.preferences.device.DevicePreferencesView;
import com.valterc.ki2.data.shifting.ShiftingInfo;
import com.valterc.ki2.karoo.Ki2Context;
import com.valterc.ki2.karoo.overlay.OverlayPreferences;
import com.valterc.ki2.karoo.overlay.OverlayTriggers;
import com.valterc.ki2.karoo.overlay.position.PositionManager;
import com.valterc.ki2.karoo.overlay.view.IOverlayView;
import com.valterc.ki2.karoo.overlay.view.builder.OverlayViewBuilderEntry;
import com.valterc.ki2.karoo.overlay.view.builder.OverlayViewBuilderRegistry;
import com.valterc.ki2.utils.ActivityUtils;

import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

@SuppressLint("LogNotTimber")
public abstract class BaseOverlayManager {

    private static final int DURATION_ALWAYS_VISIBLE = -1;

    private final Ki2Context ki2Context;
    private final LayoutInflater layoutInflater;
    private final Handler handler;

    private RelativeLayout parentLayout;
    private PositionManager positionManager;
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

    private Consumer<Boolean> visibilityListener;

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

    public BaseOverlayManager(Ki2Context ki2Context) {
        this.ki2Context = ki2Context;
        this.handler = ki2Context.getHandler();
        this.layoutInflater = LayoutInflater.from(ki2Context.getSdkContext()).cloneInContext(ki2Context.getSdkContext());

        ki2Context.getServiceClient().registerPreferencesWeakListener(preferencesListener);
        ki2Context.getServiceClient().registerShiftingInfoWeakListener(shiftingInfoListener);
        ki2Context.getServiceClient().registerDevicePreferencesWeakListener(devicePreferencesListener);
        ki2Context.getServiceClient().registerBatteryInfoWeakListener(batteryInfoListener);
        ki2Context.getServiceClient().registerConnectionInfoWeakListener(connectionInfoListener);
    }

    protected Ki2Context getKi2Context() {
        return ki2Context;
    }

    protected PreferencesView getPreferences() {
        return preferences;
    }

    public void setVisibilityListener(Consumer<Boolean> visibilityListener) {
        this.visibilityListener = visibilityListener;
    }

    private void updatePreferences() {
        overlayEnabled = isOverlayEnabled();
        overlayDuration = getOverlayDuration();
        overlayTriggers = new OverlayTriggers(getOverlayTriggersSet());
    }

    public OverlayTriggers getOverlayTriggers(){
        return overlayTriggers;
    }

    protected abstract Set<String> getOverlayTriggersSet();

    protected abstract int getOverlayDuration();

    protected abstract boolean isOverlayEnabled();

    protected abstract int getOverlayPositionY();

    protected abstract int getOverlayPositionX();

    protected abstract String getOverlayTheme();

    protected abstract OverlayPreferences getOverlayPreferences();

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
            return false;
        }

        ViewGroup viewGroupBase = (ViewGroup) viewBase;
        OverlayViewBuilderEntry viewBuilder = OverlayViewBuilderRegistry.getBuilder(getOverlayTheme());

        if (viewBuilder != null) {
            parentLayout = (RelativeLayout) layoutInflater.inflate(R.layout.view_karoo_overlay_parent, viewGroupBase, false);
            viewGroupBase.addView(parentLayout);

            View viewOverlay = layoutInflater.inflate(viewBuilder.getLayoutId(), parentLayout, false);
            parentLayout.addView(viewOverlay);

            view = viewBuilder.createOverlayView(ki2Context, viewOverlay);
            view.applyPreferences(ki2Context, getOverlayPreferences());
            view.setupInRide();
            positionManager = new PositionManager(getOverlayPositionX(), getOverlayPositionY(), viewOverlay);
            positionManager.updatePosition();

            view.setVisibilityListener(v -> {
                if (visibilityListener != null) {
                    visibilityListener.accept(v);
                }
            });

            view.hide();
        }

        activityHashCode = activity.hashCode();
        return true;
    }

    private void removeView() {
        if (parentLayout != null) {
            ViewParent parent = parentLayout.getParent();
            if (parent != null) {
                ((ViewGroup) parent).removeView(parentLayout);
            }
        }

        if (view != null) {
            view.remove();
            view = null;
        }
    }

    protected void showOverlay(boolean force) {
        if (!overlayEnabled || preferences == null) {
            return;
        }

        if (!ensureView()) {
            return;
        }

        if (connectionInfo == null || devicePreferences == null ||
                (!force && !overlayTriggers.queryAndClearShouldShowOverlay())) {
            return;
        }

        timestampLastTrigger = System.currentTimeMillis();
        view.updateView(preferences, connectionInfo, devicePreferences, batteryInfo, shiftingInfo);
        positionManager.updatePosition();
        view.show();

        if (overlayDuration != DURATION_ALWAYS_VISIBLE) {
            handler.postDelayed(() -> {
                if (System.currentTimeMillis() - timestampLastTrigger >= overlayDuration) {
                    if (view != null) {
                        view.hide();
                    }
                }
            }, overlayDuration);
        }
    }

    protected void hideOverlay() {
        if (view != null) {
            view.hide();
        }
    }

}
