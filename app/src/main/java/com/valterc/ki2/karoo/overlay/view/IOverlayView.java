package com.valterc.ki2.karoo.overlay.view;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.valterc.ki2.data.connection.ConnectionInfo;
import com.valterc.ki2.data.device.BatteryInfo;
import com.valterc.ki2.data.preferences.PreferencesView;
import com.valterc.ki2.data.preferences.device.DevicePreferencesView;
import com.valterc.ki2.data.shifting.ShiftingInfo;

public interface IOverlayView {

    /**
     * Setup the overlay in ride.
     */
    void setupInRide();

    /**
     * Remove overlay from view hierarchy.
     */
    void remove();

    /**
     * Show overlay.
     */
    void show();

    /**
     * Hide overlay.
     */
    void hide();

    /**
     * Set overlay view alpha.
     *
     * @param value Overlay alpha.
     */
    void setAlpha(float value);

    /**
     * Update overlay view state.
     *
     * @param preferences Preferences view.
     * @param connectionInfo Device connection info.
     * @param devicePreferences Device preferences view.
     * @param batteryInfo Device battery info.
     * @param shiftingInfo Device shifting info.
     */
    void updateView(@NonNull PreferencesView preferences,
                    @NonNull ConnectionInfo connectionInfo,
                    @NonNull DevicePreferencesView devicePreferences,
                    @Nullable BatteryInfo batteryInfo,
                    @Nullable ShiftingInfo shiftingInfo);


}
