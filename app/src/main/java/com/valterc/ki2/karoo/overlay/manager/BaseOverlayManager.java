package com.valterc.ki2.karoo.overlay.manager;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.PixelFormat;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

import com.valterc.ki2.R;
import com.valterc.ki2.data.connection.ConnectionInfo;
import com.valterc.ki2.data.device.BatteryInfo;
import com.valterc.ki2.data.device.DeviceId;
import com.valterc.ki2.data.preferences.PreferencesView;
import com.valterc.ki2.data.preferences.device.DevicePreferencesView;
import com.valterc.ki2.data.shifting.ShiftingInfo;
import com.valterc.ki2.karoo.Ki2ExtensionContext;
import com.valterc.ki2.karoo.overlay.OverlayPreferences;
import com.valterc.ki2.karoo.overlay.OverlayTriggers;
import com.valterc.ki2.karoo.overlay.position.PositionManager;
import com.valterc.ki2.karoo.overlay.view.IOverlayView;
import com.valterc.ki2.karoo.overlay.view.builder.OverlayViewBuilderEntry;
import com.valterc.ki2.karoo.overlay.view.builder.OverlayViewBuilderRegistry;

import java.util.Collections;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import timber.log.Timber;

@SuppressLint("LogNotTimber")
public abstract class BaseOverlayManager {

    private static final int DURATION_ALWAYS_VISIBLE = -1;
    private final Ki2ExtensionContext extensionContext;

    private final WindowManager windowManager;
    private final LayoutInflater layoutInflater;
    private final Handler handler;

    private FrameLayout parentViewGroup;
    private RelativeLayout parentLayout;
    private PositionManager positionManager;
    private IOverlayView view;

    private OverlayTriggers overlayTriggers;
    private long timestampLastTrigger;
    private boolean overlayEnabled;
    private int overlayDuration;
    private boolean disposed;

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

    public BaseOverlayManager(Ki2ExtensionContext extensionContext) {
        this.extensionContext = extensionContext;
        this.handler = new Handler(Looper.getMainLooper());
        this.windowManager = (WindowManager) extensionContext.getContext().getSystemService(Context.WINDOW_SERVICE);
        this.layoutInflater = LayoutInflater.from(extensionContext.getContext());
        this.overlayTriggers = new OverlayTriggers(Collections.emptySet());

        extensionContext.getServiceClient().registerPreferencesWeakListener(preferencesListener);
        extensionContext.getServiceClient().registerShiftingInfoWeakListener(shiftingInfoListener);
        extensionContext.getServiceClient().registerDevicePreferencesWeakListener(devicePreferencesListener);
        extensionContext.getServiceClient().registerBatteryInfoWeakListener(batteryInfoListener);
        extensionContext.getServiceClient().registerConnectionInfoWeakListener(connectionInfoListener);
    }

    protected Ki2ExtensionContext getExtensionContext() {
        return extensionContext;
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

    public OverlayTriggers getOverlayTriggers() {
        return overlayTriggers;
    }

    public void dispose() {
        disposed = true;
        removeView();
    }

    protected abstract Set<String> getOverlayTriggersSet();

    protected abstract int getOverlayDuration();

    protected abstract boolean isOverlayEnabled();

    protected abstract int getOverlayPositionY();

    protected abstract int getOverlayPositionX();

    protected abstract String getOverlayTheme();

    protected abstract OverlayPreferences getOverlayPreferences();

    @SuppressLint("InflateParams")
    private void ensureView() {
        if (view != null) {
            return;
        }

        parentViewGroup = (FrameLayout) layoutInflater.inflate(R.layout.window, null);

        var layoutParams = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                        | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                        | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
                PixelFormat.TRANSLUCENT
        );

        disableMoveAnimation(layoutParams);

        layoutParams.gravity = Gravity.TOP;
        layoutParams.y = 0;

        windowManager.addView(parentViewGroup, layoutParams);

        OverlayViewBuilderEntry viewBuilder = OverlayViewBuilderRegistry.getBuilder(getOverlayTheme());

        if (viewBuilder != null) {
            parentLayout = (RelativeLayout) layoutInflater.inflate(R.layout.view_karoo_overlay_parent, parentViewGroup, false);
            parentViewGroup.addView(parentLayout);

            View viewOverlay = layoutInflater.inflate(viewBuilder.getLayoutId(), parentLayout, false);
            parentLayout.addView(viewOverlay);

            view = viewBuilder.createOverlayView(extensionContext, preferences, viewOverlay);
            view.applyPreferences(extensionContext, getOverlayPreferences());
            view.setupInRide();
            positionManager = new PositionManager(getOverlayPositionX(), getOverlayPositionY(), windowManager, viewOverlay, parentViewGroup);
            positionManager.updatePosition();

            view.setVisibilityListener(v -> {
                if (visibilityListener != null) {
                    visibilityListener.accept(v);
                }
            });

            view.hide();
        }
    }

    private static void disableMoveAnimation(WindowManager.LayoutParams layoutParams) {
        try {
            var field = layoutParams.getClass().getField("privateFlags");
            Integer currentFlags = (Integer) field.get(layoutParams);
            if (currentFlags == null) {
                currentFlags = 0;
            }

            field.set(layoutParams, currentFlags | 0x00000040);
        } catch (Exception e) {
            Timber.w(e, "Unable to disable move animation");
        }
    }

    private void removeView() {
        if (parentViewGroup != null) {
            windowManager.removeView(parentViewGroup);
        }

        if (parentLayout != null && parentViewGroup != null) {
            parentViewGroup.removeView(parentLayout);
        }

        if (view != null) {
            view.remove();
            view = null;
        }

        parentViewGroup = null;
        parentLayout = null;
    }

    protected void showOverlay(boolean force) {
        if (disposed || !overlayEnabled || preferences == null) {
            return;
        }

        ensureView();

        if (connectionInfo == null || devicePreferences == null ||
                (!force && !overlayTriggers.queryAndClearShouldShowOverlay())) {
            return;
        }

        timestampLastTrigger = System.currentTimeMillis();
        view.updateView(connectionInfo, devicePreferences, batteryInfo, shiftingInfo);
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

    protected void toggleOverlay() {
        if (view != null && view.isVisible()) {
            hideOverlay();
        } else {
            showOverlay(true);
        }
    }

    protected void refreshOverlay() {
        handler.post(() -> {
            parentViewGroup.setVisibility(View.INVISIBLE);
            handler.post(() -> parentViewGroup.setVisibility(View.VISIBLE));
        });
    }

}
