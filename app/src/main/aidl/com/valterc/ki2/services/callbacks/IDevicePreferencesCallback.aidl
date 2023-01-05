// IDevicePreferencesCallback.aidl
package com.valterc.ki2.services.callbacks;

import com.valterc.ki2.data.device.DeviceId;
import com.valterc.ki2.data.preferences.device.DevicePreferencesView;

interface IDevicePreferencesCallback {

    void onDevicePreferences(in DeviceId deviceId, in DevicePreferencesView devicePreferencesView);

}