package com.valterc.ki2.data.device;

import android.content.Context;

import androidx.annotation.NonNull;

import com.valterc.ki2.R;


public final class DeviceName {

    private DeviceName() {
    }

    /**
     * Get default device name.
     *
     * @param context  Application context.
     * @param deviceId Device identifier.
     * @return Default device name.
     */
    @NonNull
    public static String getDefaultName(Context context, DeviceId deviceId) {
        if (deviceId.getDeviceType() == DeviceType.SHIMANO_SHIFTING) {
            return context.getString(R.string.text_param_di2_name, deviceId.getName());
        } else if (deviceId.getDeviceType() == DeviceType.MOCK_SHIFTING) {
            return context.getString(R.string.text_param_mock_name, deviceId.getName());
        } else {
            return context.getString(R.string.text_param_sensor_name, deviceId.getName());
        }
    }

}
