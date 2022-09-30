// IBatteryCallback.aidl
package com.valterc.ki2.services.callbacks;

import com.valterc.ki2.data.device.DeviceId;
import com.valterc.ki2.data.device.BatteryInfo;

interface IBatteryCallback {

    void onBattery(in DeviceId deviceId, in BatteryInfo batteryInfo);
}