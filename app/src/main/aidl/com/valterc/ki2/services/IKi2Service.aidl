// IKi2Service.aidl
package com.valterc.ki2.services;

import com.valterc.ki2.services.callbacks.IConnectionDataInfoCallback;
import com.valterc.ki2.services.callbacks.IConnectionInfoCallback;
import com.valterc.ki2.services.callbacks.IShiftingCallback;
import com.valterc.ki2.services.callbacks.IBatteryCallback;
import com.valterc.ki2.services.callbacks.IManufacturerInfoCallback;
import com.valterc.ki2.services.callbacks.ISwitchKeyCallback;
import com.valterc.ki2.services.callbacks.ISwitchCallback;
import com.valterc.ki2.services.callbacks.IScanCallback;

import com.valterc.ki2.data.device.DeviceId;

interface IKi2Service {

    void registerConnectionDataInfoListener(IConnectionDataInfoCallback callback);
    void unregisterConnectionDataInfoListener(IConnectionDataInfoCallback callback);

    void registerConnectionInfoListener(IConnectionInfoCallback callback);
    void unregisterConnectionInfoListener(IConnectionInfoCallback callback);

    void registerShiftingListener(IShiftingCallback callback);
    void unregisterShiftingListener(IShiftingCallback callback);

    void registerBatteryListener(IBatteryCallback callback);
    void unregisterBatteryListener(IBatteryCallback callback);

    void registerManufacturerInfoListener(IManufacturerInfoCallback callback);
    void unregisterManufacturerInfoListener(IManufacturerInfoCallback callback);

    void registerSwitchKeyListener(ISwitchKeyCallback callback);
    void unregisterSwitchKeyListener(ISwitchKeyCallback callback);

    void registerSwitchListener(ISwitchCallback callback);
    void unregisterSwitchListener(ISwitchCallback callback);

    void registerScanListener(IScanCallback callback);
    void unregisterScanListener(IScanCallback callback);

    void changeShiftMode(in DeviceId deviceId);
    void reconnectDevice(in DeviceId deviceId);

    void saveDevice(in DeviceId deviceId);
    void deleteDevice(in DeviceId deviceId);
    List<DeviceId> getSavedDevices();

}