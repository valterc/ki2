package com.valterc.ki2.karoo.overlay.view.compact;

import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.valterc.ki2.R;
import com.valterc.ki2.data.connection.ConnectionInfo;
import com.valterc.ki2.data.device.BatteryInfo;
import com.valterc.ki2.data.preferences.PreferencesView;
import com.valterc.ki2.data.preferences.device.DevicePreferencesView;
import com.valterc.ki2.data.shifting.ShiftingInfo;
import com.valterc.ki2.karoo.Ki2ExtensionContext;

public class CompactDarkGearSizeRatioOverlayView extends CompactDarkOverlayView {

    public CompactDarkGearSizeRatioOverlayView(Ki2ExtensionContext context, PreferencesView preferences, View view) {
        super(context, preferences, view);

        getViewHolder().getLinearLayoutGearingRatio().setVisibility(View.GONE);
    }

    @Override
    public void updateView(@NonNull ConnectionInfo connectionInfo,
                           @NonNull DevicePreferencesView devicePreferences,
                           @Nullable BatteryInfo batteryInfo,
                           @Nullable ShiftingInfo shiftingInfo) {
        super.updateView(connectionInfo, devicePreferences, batteryInfo, shiftingInfo);

        if (!shiftingGearingHelper.hasInvalidGearingInfo() && connectionInfo.isConnected()) {
            getViewHolder().getTextViewGearing().setText(
                    getContext().getString(R.string.text_param_gearing,
                            shiftingGearingHelper.getFrontGearTeethCount(),
                            shiftingGearingHelper.getRearGearTeethCount()));
            getViewHolder().getTextViewGearingRatio().setText(getContext().getString(R.string.text_param_ratio, shiftingGearingHelper.getGearRatio()));
            getViewHolder().getLinearLayoutGearingRatio().setVisibility(View.VISIBLE);
        }
    }

}
