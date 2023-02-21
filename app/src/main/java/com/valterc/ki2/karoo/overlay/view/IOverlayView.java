package com.valterc.ki2.karoo.overlay.view;

import com.valterc.ki2.data.device.BatteryInfo;
import com.valterc.ki2.data.preferences.device.DevicePreferencesView;
import com.valterc.ki2.data.shifting.ShiftingInfo;

public interface IOverlayView {

    void remove();

    void show();

    void hide();

    void updateView(ShiftingInfo shiftingInfo, BatteryInfo batteryInfo, DevicePreferencesView devicePreferences);

}
