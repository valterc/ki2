// IKi2Service.aidl
package com.valterc.ki2.services;

import com.valterc.ki2.services.callbacks.IConnectionDataInfoCallback;
import com.valterc.ki2.services.callbacks.IConnectionInfoCallback;
import com.valterc.ki2.services.callbacks.IShiftingCallback;
import com.valterc.ki2.services.callbacks.IBatteryCallback;
import com.valterc.ki2.services.callbacks.IManufacturerInfoCallback;
import com.valterc.ki2.services.callbacks.IKeyCallback;
import com.valterc.ki2.services.callbacks.ISwitchCallback;
import com.valterc.ki2.services.callbacks.IScanCallback;
import com.valterc.ki2.services.callbacks.IMessageCallback;
import com.valterc.ki2.services.callbacks.IPreferencesCallback;
import com.valterc.ki2.services.callbacks.IDevicePreferencesCallback;

import com.valterc.ki2.data.device.DeviceId;
import com.valterc.ki2.data.device.DeviceInfo;
import com.valterc.ki2.data.message.Message;
import com.valterc.ki2.data.preferences.PreferencesView;
import com.valterc.ki2.data.preferences.device.DevicePreferencesView;

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

    void registerKeyListener(IKeyCallback callback);
    void unregisterKeyListener(IKeyCallback callback);

    void registerSwitchListener(ISwitchCallback callback);
    void unregisterSwitchListener(ISwitchCallback callback);

    void registerScanListener(IScanCallback callback);
    void unregisterScanListener(IScanCallback callback);

    void registerMessageListener(IMessageCallback callback);
    void unregisterMessageListener(IMessageCallback callback);

    void registerPreferencesListener(IPreferencesCallback callback);
    void unregisterPreferencesListener(IPreferencesCallback callback);

    void registerDevicePreferencesListener(IDevicePreferencesCallback callback);
    void unregisterDevicePreferencesListener(IDevicePreferencesCallback callback);

    void sendMessage(in Message message);
    void clearMessage(String key);
    void clearMessages();
    List<Message> getMessages();

    PreferencesView getPreferences();
    DevicePreferencesView getDevicePreferences(in DeviceId deviceId);

    void restartDeviceScan();
    void restartDeviceConnections();

    void changeShiftMode(in DeviceId deviceId);
    void reconnectDevice(in DeviceId deviceId);

    void saveDevice(in DeviceId deviceId);
    void deleteDevice(in DeviceId deviceId);
    List<DeviceId> getSavedDevices();

}