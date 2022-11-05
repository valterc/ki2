package com.valterc.ki2.data.message;

import android.os.Bundle;

import com.valterc.ki2.data.device.DeviceId;

public class LowBatteryMessage extends Message {

    private static final String KEY_DEVICE_ID = "deviceId";
    private static final String KEY_BATTERY_PERCENTAGE = "batteryPercentage";

    public static String getKey(DeviceId deviceId) {
        return "low-battery-" + deviceId.getName();
    }

    public static LowBatteryMessage parse(Message message) {
        if (!message.getClassType().equals(LowBatteryMessage.class.getName())) {
            return null;
        }

        return new LowBatteryMessage(message);
    }

    private final DeviceId deviceId;
    private final int batteryPercentage;

    private LowBatteryMessage(Message message) {
        super(message);

        getBundle().setClassLoader(getClass().getClassLoader());
        deviceId = getBundle().getParcelable(KEY_DEVICE_ID);
        batteryPercentage = getBundle().getInt(KEY_BATTERY_PERCENTAGE);
    }

    public LowBatteryMessage(DeviceId deviceId, int batteryPercentage) {
        super(getKey(deviceId), new Bundle(), MessageType.LOW_BATTERY, true);

        getBundle().putParcelable(KEY_DEVICE_ID, deviceId);
        getBundle().putInt(KEY_BATTERY_PERCENTAGE, batteryPercentage);

        this.deviceId = deviceId;
        this.batteryPercentage = batteryPercentage;
    }

    public DeviceId getDeviceId() {
        return deviceId;
    }

    public int getBatteryPercentage() {
        return batteryPercentage;
    }
}
