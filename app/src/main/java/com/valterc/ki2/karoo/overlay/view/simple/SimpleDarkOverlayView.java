package com.valterc.ki2.karoo.overlay.view.simple;

import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.valterc.ki2.data.connection.ConnectionInfo;
import com.valterc.ki2.data.device.BatteryInfo;
import com.valterc.ki2.data.preferences.PreferencesView;
import com.valterc.ki2.data.preferences.device.DevicePreferencesView;
import com.valterc.ki2.data.shifting.ShiftingInfo;
import com.valterc.ki2.karoo.Ki2ExtensionContext;
import com.valterc.ki2.karoo.overlay.view.plain.DefaultDarkOverlayView;

public class SimpleDarkOverlayView extends DefaultDarkOverlayView {

    public SimpleDarkOverlayView(Ki2ExtensionContext context, PreferencesView preferences, View view) {
        super(context, preferences, view);

        getViewHolder().getLinearLayoutDetails().setVisibility(View.GONE);
    }

    @Override
    public void updateView(@NonNull ConnectionInfo connectionInfo,
                           @NonNull DevicePreferencesView devicePreferences,
                           @Nullable BatteryInfo batteryInfo,
                           @Nullable ShiftingInfo shiftingInfo) {
        super.updateView(connectionInfo, devicePreferences, batteryInfo, shiftingInfo);

        if (connectionInfo.isConnected()) {
            getViewHolder().getLinearLayoutDetails().setVisibility(View.GONE);
        } else {
            getViewHolder().getLinearLayoutDetails().setVisibility(View.VISIBLE);
        }
    }
}
