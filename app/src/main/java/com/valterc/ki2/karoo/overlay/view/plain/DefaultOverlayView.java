package com.valterc.ki2.karoo.overlay.view.plain;

import android.content.Context;
import android.view.View;

import com.valterc.ki2.R;
import com.valterc.ki2.data.device.BatteryInfo;
import com.valterc.ki2.data.preferences.device.DevicePreferencesView;
import com.valterc.ki2.data.shifting.BuzzerType;
import com.valterc.ki2.data.shifting.ShiftingInfo;
import com.valterc.ki2.karoo.overlay.view.BaseOverlayView;

public abstract class DefaultOverlayView extends BaseOverlayView<DefaultOverlayViewHolder> {

    public DefaultOverlayView(Context context, View view) {
        super(context, new DefaultOverlayViewHolder(view));
    }

    @Override
    public void updateView(ShiftingInfo shiftingInfo, BatteryInfo batteryInfo, DevicePreferencesView devicePreferences) {
        getViewHolder().getTextViewDeviceName().setText(devicePreferences.getName(getContext()));

        getViewHolder().getBatteryView().setValue((float) batteryInfo.getValue() / 100);
        getViewHolder().getTextViewBattery().setText(getContext().getString(R.string.text_param_percentage, batteryInfo.getValue()));

        getViewHolder().getGearsView().setGears(
                shiftingInfo.getFrontGearMax(),
                shiftingInfo.getFrontGear(),
                shiftingInfo.getRearGearMax(),
                shiftingInfo.getRearGear());

        getViewHolder().getTextViewGearing().setText(
                getContext().getString(R.string.text_param_gearing_and_ratio,
                        shiftingInfo.getFrontGear(),
                        shiftingInfo.getRearGear(),
                        1.2997f));

        if (shiftingInfo.getBuzzerType() == BuzzerType.UPCOMING_SYNCHRO_SHIFT) {
            getViewHolder().getTextViewGearingExtra().setText(R.string.text_synchro);
        } else if (shiftingInfo.getBuzzerType() == BuzzerType.OVERLIMIT_PROTECTION) {
            getViewHolder().getTextViewGearingExtra().setText(R.string.text_limit);
        } else {
            getViewHolder().getTextViewGearingExtra().setText(shiftingInfo.getShiftingMode().getMode());
        }
    }
}
