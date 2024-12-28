package com.valterc.ki2.karoo.overlay.view.compact;

import android.os.Handler;
import android.os.Looper;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.valterc.ki2.R;
import com.valterc.ki2.data.connection.ConnectionInfo;
import com.valterc.ki2.data.device.BatteryInfo;
import com.valterc.ki2.data.preferences.PreferencesView;
import com.valterc.ki2.data.preferences.device.DevicePreferencesView;
import com.valterc.ki2.data.shifting.ShiftingInfo;
import com.valterc.ki2.data.shifting.UpcomingSynchroShiftType;
import com.valterc.ki2.karoo.Ki2ExtensionContext;
import com.valterc.ki2.karoo.overlay.view.BaseOverlayView;
import com.valterc.ki2.karoo.shifting.BuzzerTracking;
import com.valterc.ki2.karoo.shifting.ShiftingGearingHelper;

public abstract class CompactOverlayView extends BaseOverlayView<CompactOverlayViewHolder> {

    private static final int TIME_MS_BLINKING = 500;

    private static final int TIME_MS_BLINKING_TIMEOUT = 2000;

    protected final ShiftingGearingHelper shiftingGearingHelper;
    private final BuzzerTracking buzzerTracking;
    private final Handler handler;
    private Runnable blinkMethod;

    private long timestampBlinkStart;

    public CompactOverlayView(Ki2ExtensionContext ki2Context, PreferencesView preferences, View view) {
        super(ki2Context.getContext(), preferences, new CompactOverlayViewHolder(view));
        shiftingGearingHelper = new ShiftingGearingHelper(ki2Context.getContext());
        buzzerTracking = new BuzzerTracking();
        handler = new Handler(Looper.getMainLooper());
    }

    @Override
    public void setupInRide() {
        getViewHolder().getOverlayView().setClickable(true);
        getViewHolder().getOverlayView().setOnClickListener(v -> hide());
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
            getViewHolder().getTextViewGearingExtra().setVisibility(View.GONE);
            getViewHolder().getLinearLayoutGearingRatio().setVisibility(View.GONE);

            if (connectionInfo.isNewOrConnecting()) {
                getViewHolder().getTextViewGearing().setText("...");
            } else {
                getViewHolder().getTextViewGearing().setText("N/A");
            }
        } else {
            if (shiftingInfo == null) {
                stopBlinking();
                getViewHolder().getTextViewGearingExtra().setVisibility(View.GONE);
            } else {
                if (buzzerTracking.isUpcomingSynchroShift()) {
                    startBlinking();

                    if (buzzerTracking.getUpcomingSynchroShiftType() == UpcomingSynchroShiftType.UPCOMING_UP) {
                        getViewHolder().getTextViewGearingExtra().setText(R.string.text_synchro_up);
                    } else if (buzzerTracking.getUpcomingSynchroShiftType() == UpcomingSynchroShiftType.UPCOMING_DOWN) {
                        getViewHolder().getTextViewGearingExtra().setText(R.string.text_synchro_down);
                    }
                } else if (buzzerTracking.isShiftingLimit()) {
                    startBlinking();
                    getViewHolder().getTextViewGearingExtra().setText(R.string.text_limit);
                } else {
                    stopBlinking();
                    getViewHolder().getTextViewGearingExtra().setVisibility(View.GONE);
                }
            }
        }
    }

    private void startBlinking() {
        timestampBlinkStart = System.currentTimeMillis();
        if (blinkMethod == null) {
            blinkMethod = this::blink;
            getViewHolder().getTextViewGearingExtra().setVisibility(View.VISIBLE);
            handler.postDelayed(blinkMethod, TIME_MS_BLINKING);
        }
    }

    private void stopBlinking() {
        if (blinkMethod != null) {
            handler.removeCallbacks(blinkMethod);
            blinkMethod = null;
        }

        getViewHolder().getTextViewGearingExtra().setVisibility(View.VISIBLE);
    }

    private void blink() {
        if (getViewHolder().getTextViewGearingExtra().getVisibility() == View.VISIBLE) {
            getViewHolder().getTextViewGearingExtra().setVisibility(View.INVISIBLE);
        } else {
            getViewHolder().getTextViewGearingExtra().setVisibility(View.VISIBLE);
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
