package com.valterc.ki2.karoo.overlay.view.slim;

import android.os.Handler;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.valterc.ki2.R;
import com.valterc.ki2.data.connection.ConnectionInfo;
import com.valterc.ki2.data.device.BatteryInfo;
import com.valterc.ki2.data.preferences.PreferencesView;
import com.valterc.ki2.data.preferences.device.DevicePreferencesView;
import com.valterc.ki2.data.shifting.ShiftingInfo;
import com.valterc.ki2.karoo.Ki2Context;
import com.valterc.ki2.karoo.overlay.view.BaseOverlayView;
import com.valterc.ki2.karoo.extension.shifting.BuzzerTracking;
import com.valterc.ki2.karoo.extension.shifting.ShiftingGearingHelper;

public abstract class SlimOverlayView extends BaseOverlayView<SlimOverlayViewHolder> {

    private static final int TIME_MS_BLINKING = 500;

    private static final int TIME_MS_BLINKING_TIMEOUT = 2000;

    protected final ShiftingGearingHelper shiftingGearingHelper;
    private final BuzzerTracking buzzerTracking;
    private final Handler handler;
    private Runnable blinkMethod;

    private long timestampBlinkStart;
    private boolean canBeShown;

    public SlimOverlayView(Ki2Context ki2Context, PreferencesView preferences, View view) {
        super(ki2Context.getSdkContext(), preferences, new SlimOverlayViewHolder(view));
        shiftingGearingHelper = new ShiftingGearingHelper(ki2Context.getSdkContext());
        buzzerTracking = new BuzzerTracking();
        handler = ki2Context.getHandler();
    }

    @Override
    public void setupInRide() {
        getViewHolder().getOverlayView().setClickable(true);
        getViewHolder().getOverlayView().setOnClickListener(v -> hide());
    }

    @Override
    public void show() {
        if (canBeShown) {
            super.show();
        }
    }

    @Override
    public void hide() {
        super.hide();
        stopBlinking();
    }

    @Override
    public void updateView(@NonNull ConnectionInfo connectionInfo,
                           @NonNull DevicePreferencesView devicePreferences,
                           @Nullable BatteryInfo batteryInfo,
                           @Nullable ShiftingInfo shiftingInfo) {

        shiftingGearingHelper.setShiftingInfo(shiftingInfo);
        shiftingGearingHelper.setDevicePreferences(devicePreferences);
        buzzerTracking.setShiftingInfo(shiftingInfo);

        if (shiftingGearingHelper.hasInvalidGearingInfo() || !connectionInfo.isConnected()) {
            getViewHolder().getOverlayView().setVisibility(View.INVISIBLE);
            canBeShown = false;
        } else {
            getViewHolder().getOverlayView().setVisibility(View.VISIBLE);
            canBeShown = true;
            getViewHolder().getSlimGearsView().setGears(
                    shiftingGearingHelper.getFrontGearMax(),
                    shiftingGearingHelper.getFrontGear(),
                    shiftingGearingHelper.getRearGearMax(),
                    shiftingGearingHelper.getRearGear());

            if (shiftingInfo == null) {
                stopBlinking();
            } else {
                if (buzzerTracking.isUpcomingSynchroShift()) {
                    startBlinking();
                } else if (buzzerTracking.isShiftingLimit()) {
                    startBlinking();
                } else {
                    stopBlinking();
                }
            }
        }
    }

    private void startBlinking() {
        timestampBlinkStart = System.currentTimeMillis();
        if (blinkMethod == null) {
            blinkMethod = this::blink;
            getViewHolder().getSlimGearsView().setSelectedRearGearColor(getContext().getColor(R.color.red_faded));
            handler.postDelayed(blinkMethod, TIME_MS_BLINKING);
        }
    }

    private void stopBlinking() {
        if (blinkMethod != null) {
            handler.removeCallbacks(blinkMethod);
            blinkMethod = null;
        }

        getViewHolder().getSlimGearsView().setSelectedRearGearColor(getContext().getColor(R.color.red_faded));
    }

    private void blink() {
        if (getViewHolder().getSlimGearsView().getSelectedRearGearColor() == getContext().getColor(R.color.red_faded)) {
            getViewHolder().getSlimGearsView().setSelectedRearGearColor(getViewHolder().getSlimGearsView().getGearColor());
        } else {
            getViewHolder().getSlimGearsView().setSelectedRearGearColor(getContext().getColor(R.color.red_faded));
        }

        if (timestampBlinkStart + TIME_MS_BLINKING_TIMEOUT < System.currentTimeMillis()) {
            stopBlinking();
        } else {
            if (blinkMethod != null) {
                handler.postDelayed(blinkMethod, TIME_MS_BLINKING);
            }
        }
    }

}
