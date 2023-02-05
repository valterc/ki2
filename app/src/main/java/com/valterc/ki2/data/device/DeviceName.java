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
        String name = String.valueOf(deviceId.getDeviceNumber());
        switch (deviceId.getDeviceType()) {
            case SHIMANO_SHIFTING:
                return context.getString(R.string.text_param_di2_name, name);

            case SHIMANO_EBIKE:
                return context.getString(R.string.text_param_steps_name, name);

            case MOCK_SHIFTING:
                return context.getString(R.string.text_param_mock_name, name);

            case UNKNOWN:
            default:
                return context.getString(R.string.text_param_sensor_name, name);
        }
    }

}
