package com.valterc.ki2.karoo.overlay.view.plain;

import android.os.Handler;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.valterc.ki2.R;
import com.valterc.ki2.data.connection.ConnectionInfo;
import com.valterc.ki2.data.device.BatteryInfo;
import com.valterc.ki2.data.preferences.PreferencesView;
import com.valterc.ki2.data.preferences.device.DevicePreferencesView;
import com.valterc.ki2.data.shifting.BuzzerType;
import com.valterc.ki2.data.shifting.ShiftingInfo;
import com.valterc.ki2.data.shifting.UpcomingSynchroShiftType;
import com.valterc.ki2.karoo.Ki2Context;
import com.valterc.ki2.karoo.overlay.view.BaseOverlayView;
import com.valterc.ki2.karoo.shifting.ShiftingGearingHelper;
import com.valterc.ki2.karoo.shifting.SynchroShiftTracking;

public abstract class DefaultOverlayView extends BaseOverlayView<DefaultOverlayViewHolder> {

    private static final int TIME_MS_BLINKING = 500;

    protected final ShiftingGearingHelper shiftingGearingHelper;
    private final SynchroShiftTracking synchroShiftTracking;
    private final Handler handler;
    private Runnable blinkMethod;

    public DefaultOverlayView(Ki2Context ki2Context, View view) {
        super(ki2Context.getSdkContext(), new DefaultOverlayViewHolder(view));
        shiftingGearingHelper = new ShiftingGearingHelper(ki2Context.getSdkContext());
        synchroShiftTracking = new SynchroShiftTracking();
        handler = ki2Context.getHandler();
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
    public void updateView(@NonNull PreferencesView preferences,
                           @NonNull ConnectionInfo connectionInfo,
                           @NonNull DevicePreferencesView devicePreferences,
                           @Nullable BatteryInfo batteryInfo,
                           @Nullable ShiftingInfo shiftingInfo) {
        shiftingGearingHelper.setShiftingInfo(shiftingInfo);
        shiftingGearingHelper.setDevicePreferences(devicePreferences);
        synchroShiftTracking.setShiftingInfo(shiftingInfo);

        getViewHolder().getTextViewDeviceName().setText(devicePreferences.getName(getContext()));

        if (batteryInfo == null) {
            getViewHolder().getBatteryView().setVisibility(View.GONE);
            getViewHolder().getTextViewBattery().setVisibility(View.GONE);
        } else {
            getViewHolder().getBatteryView().setValue((float) batteryInfo.getValue() / 100);
            getViewHolder().getTextViewBattery().setText(getContext().getString(R.string.text_param_percentage, batteryInfo.getValue()));

            if (preferences.getBatteryLevelCritical(getContext()) != null &&
                    batteryInfo.getValue() <= preferences.getBatteryLevelCritical(getContext())) {
                getViewHolder().getBatteryView().setForegroundColor(getContext().getColor(R.color.hh_red));
                getViewHolder().getBatteryView().setBorderColor(getContext().getColor(R.color.hh_red));
            } else if (preferences.getBatteryLevelLow(getContext()) != null &&
                    batteryInfo.getValue() <= preferences.getBatteryLevelLow(getContext())) {
                getViewHolder().getBatteryView().setForegroundColor(getContext().getColor(R.color.hh_red));
                getViewHolder().getBatteryView().setBorderColor(getBatteryBorderColor());
            } else {
                getViewHolder().getBatteryView().setForegroundColor(getContext().getColor(R.color.hh_green));
                getViewHolder().getBatteryView().setBorderColor(getBatteryBorderColor());
            }

            getViewHolder().getBatteryView().setVisibility(View.VISIBLE);
            getViewHolder().getTextViewBattery().setVisibility(View.VISIBLE);
        }

        if (shiftingGearingHelper.hasInvalidGearingInfo() || !connectionInfo.isConnected()) {
            getViewHolder().getGearsView().setVisibility(View.GONE);
            getViewHolder().getTextViewGearingExtra().setVisibility(View.GONE);
            getViewHolder().getLinearLayoutGearingRatio().setVisibility(View.GONE);

            switch (connectionInfo.getConnectionStatus()) {
                case NEW:
                case CONNECTING:
                    getViewHolder().getTextViewGearing().setText(R.string.text_connecting);
                    break;

                case ESTABLISHED:
                    getViewHolder().getTextViewGearing().setText(R.string.text_connected);
                    break;

                case CLOSED:
                    getViewHolder().getTextViewGearing().setText(R.string.text_disconnected);
                    break;
            }
        } else {
            getViewHolder().getGearsView().setGears(
                    shiftingGearingHelper.getFrontGearMax(),
                    shiftingGearingHelper.getFrontGear(),
                    shiftingGearingHelper.getRearGearMax(),
                    shiftingGearingHelper.getRearGear());

            if (shiftingInfo == null) {
                stopBlinking();
                getViewHolder().getTextViewGearingExtra().setVisibility(View.GONE);
            } else {
                if (synchroShiftTracking.isUpcomingSynchroShift()) {
                    startBlinking();

                    if (synchroShiftTracking.getUpcomingSynchroShiftType() == UpcomingSynchroShiftType.UPCOMING_UP) {
                        getViewHolder().getTextViewGearingExtra().setText(R.string.text_synchro_up);
                    } else if (synchroShiftTracking.getUpcomingSynchroShiftType() == UpcomingSynchroShiftType.UPCOMING_DOWN) {
                        getViewHolder().getTextViewGearingExtra().setText(R.string.text_synchro_down);
                    }
                } else if (shiftingInfo.getBuzzerType() == BuzzerType.OVERLIMIT_PROTECTION) {
                    startBlinking();
                    getViewHolder().getTextViewGearingExtra().setText(R.string.text_limit);
                } else {
                    stopBlinking();
                    getViewHolder().getTextViewGearingExtra().setText(shiftingInfo.getShiftingMode().getMode());
                }
            }

            getViewHolder().getGearsView().setVisibility(View.VISIBLE);
        }
    }

    private void startBlinking() {
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

        if (blinkMethod != null) {
            handler.postDelayed(blinkMethod, TIME_MS_BLINKING);
        }
    }

    protected abstract int getBatteryBorderColor();

}
