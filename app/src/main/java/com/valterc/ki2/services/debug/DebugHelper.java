package com.valterc.ki2.services.debug;

import com.valterc.ki2.BuildConfig;
import com.valterc.ki2.data.device.DeviceId;
import com.valterc.ki2.data.device.DeviceStore;
import com.valterc.ki2.data.device.DeviceType;

import timber.log.Timber;

public final class DebugHelper {

    private DebugHelper() {
    }

    public static void init(DeviceStore deviceStore) {
        if (!BuildConfig.DEBUG) {
            return;
        }

        Timber.i("Initializing Debug Helper");
        generateMockDevices(deviceStore);
    }

    private static void generateMockDevices(DeviceStore deviceStore) {
        deviceStore.saveDevice(new DeviceId(10100, DeviceType.MOCK_SHIFTING_VALUE, 5));
        deviceStore.saveDevice(new DeviceId(10200, DeviceType.MOCK_SHIFTING_VALUE, 5));
    }

}
