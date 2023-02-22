package com.valterc.ki2.karoo.overlay.view;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.valterc.ki2.data.connection.ConnectionInfo;
import com.valterc.ki2.data.device.BatteryInfo;
import com.valterc.ki2.data.preferences.device.DevicePreferencesView;
import com.valterc.ki2.data.shifting.ShiftingInfo;

public interface IOverlayView {

    void remove();

    void show();

    void hide();

    void updateView(@NonNull ConnectionInfo connectionInfo,
                    @NonNull DevicePreferencesView devicePreferences,
                    @Nullable BatteryInfo batteryInfo,
                    @Nullable ShiftingInfo shiftingInfo);

}
