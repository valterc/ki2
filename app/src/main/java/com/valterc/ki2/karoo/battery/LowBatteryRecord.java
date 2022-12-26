package com.valterc.ki2.karoo.battery;

import com.valterc.ki2.data.device.BatteryInfo;
import com.valterc.ki2.data.device.DeviceId;

public class LowBatteryRecord {

    private static final long TIME_MS_NOTIFICATION_TIMEOUT = 2 * 60 * 1000;

    private final DeviceId deviceId;
    private BatteryInfo batteryInfo;
    private LowBatteryCategory category;
    private long notificationTimestamp;
    private boolean notifiedInRide;

    public LowBatteryRecord(DeviceId deviceId, BatteryInfo batteryInfo, LowBatteryCategory category) {
        this.deviceId = deviceId;
        this.batteryInfo = batteryInfo;
        this.category = category;
    }

    public DeviceId getDeviceId() {
        return deviceId;
    }

    public LowBatteryCategory getCategory() {
        return category;
    }

    public BatteryInfo getBatteryInfo() {
        return batteryInfo;
    }

    public void setBatteryInfo(BatteryInfo batteryInfo) {
        this.batteryInfo = batteryInfo;
    }

    public void setCategory(LowBatteryCategory category) {
        if (this.category != category) {
            this.category = category;
            notificationTimestamp = 0;
        }
    }

    public boolean shouldNotify() {
        return shouldNotifyInRide() ||
                System.currentTimeMillis() - notificationTimestamp > TIME_MS_NOTIFICATION_TIMEOUT;
    }

    public void markNotified() {
        this.notificationTimestamp = System.currentTimeMillis();
    }

    public boolean shouldNotifyInRide() {
        return !notifiedInRide;
    }

    public void markNotifiedInRide() {
        this.notifiedInRide = true;
    }
}
