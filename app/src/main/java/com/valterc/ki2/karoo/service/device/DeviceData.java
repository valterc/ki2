package com.valterc.ki2.karoo.service.device;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.valterc.ki2.data.connection.ConnectionInfo;
import com.valterc.ki2.data.device.BatteryInfo;
import com.valterc.ki2.data.device.DeviceId;
import com.valterc.ki2.data.preferences.device.DevicePreferencesView;
import com.valterc.ki2.data.shifting.ShiftingInfo;

public class DeviceData {

    private final DeviceId deviceId;
    private ConnectionInfo connectionInfo;
    private BatteryInfo batteryInfo;
    private ShiftingInfo shiftingInfo;
    private DevicePreferencesView preferences;

    public DeviceData(@NonNull DeviceId deviceId) {
        this.deviceId = deviceId;
    }

    @NonNull
    public DeviceId getDeviceId() {
        return deviceId;
    }

    @Nullable
    public ConnectionInfo getConnectionInfo() {
        return connectionInfo;
    }

    public void setConnectionInfo(ConnectionInfo connectionInfo) {
        this.connectionInfo = connectionInfo;
    }

    @Nullable
    public BatteryInfo getBatteryInfo() {
        return batteryInfo;
    }

    public void setBatteryInfo(BatteryInfo batteryInfo) {
        this.batteryInfo = batteryInfo;
    }

    @Nullable
    public ShiftingInfo getShiftingInfo() {
        return shiftingInfo;
    }

    public void setShiftingInfo(ShiftingInfo shiftingInfo) {
        this.shiftingInfo = shiftingInfo;
    }

    @Nullable
    public DevicePreferencesView getPreferences() {
        return preferences;
    }

    public void setPreferences(DevicePreferencesView preferences) {
        this.preferences = preferences;
    }
}
