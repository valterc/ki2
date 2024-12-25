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
        return switch (deviceId.getDeviceType()) {
            case SHIMANO_SHIFTING -> context.getString(R.string.text_param_di2_name, name);
            case SHIMANO_EBIKE -> context.getString(R.string.text_param_steps_name, name);
            case MOCK_SHIFTING -> context.getString(R.string.text_param_mock_name, name);
            default -> context.getString(R.string.text_param_sensor_name, name);
        };
    }

}
