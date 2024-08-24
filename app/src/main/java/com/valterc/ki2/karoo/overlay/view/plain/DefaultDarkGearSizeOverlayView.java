package com.valterc.ki2.karoo.overlay.view.plain;

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
import com.valterc.ki2.karoo.views.KarooTheme;

public class DefaultDarkGearSizeOverlayView extends DefaultDarkOverlayView {

    public DefaultDarkGearSizeOverlayView(Ki2Context context, View view) {
        super(context, view);

        getViewHolder().getLinearLayoutGearingRatio().setVisibility(View.GONE);
        getViewHolder().getGearsView().setSelectedGearColor(new PreferencesView(context.getSdkContext()).getGearsColor(context.getSdkContext(), KarooTheme.DARK));
    }

    @Override
    public void updateView(@NonNull PreferencesView preferences,
                           @NonNull ConnectionInfo connectionInfo,
                           @NonNull DevicePreferencesView devicePreferences,
                           @Nullable BatteryInfo batteryInfo,
                           @Nullable ShiftingInfo shiftingInfo) {
        super.updateView(preferences, connectionInfo, devicePreferences, batteryInfo, shiftingInfo);

        if (!shiftingGearingHelper.hasInvalidGearingInfo() && connectionInfo.isConnected()) {
            getViewHolder().getTextViewGearing().setText(
                    getContext().getString(R.string.text_param_gearing,
                            shiftingGearingHelper.getFrontGearTeethCount(),
                            shiftingGearingHelper.getRearGearTeethCount()));
            getViewHolder().getLinearLayoutGearingRatio().setVisibility(View.GONE);
        }
    }
}
