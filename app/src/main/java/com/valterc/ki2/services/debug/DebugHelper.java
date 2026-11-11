package com.valterc.ki2.services.debug;

import com.valterc.ki2.BuildConfig;
import com.valterc.ki2.data.device.DeviceId;
import com.valterc.ki2.data.device.DeviceStore;
import com.valterc.ki2.data.device.DeviceType;

import timber.log.Timber;

public final class DebugHelper {

    public static final int MOCK_DEVICE_ID_1 = 10100;
    public static final int MOCK_DEVICE_ID_2 = 10200;
    public static final int MOCK_DEVICE_ID_3 = 10300;

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
        deviceStore.saveDevice(new DeviceId(MOCK_DEVICE_ID_1, DeviceType.MOCK_SHIFTING_VALUE, 5));
        deviceStore.saveDevice(new DeviceId(MOCK_DEVICE_ID_2, DeviceType.MOCK_SHIFTING_VALUE, 5));
        deviceStore.saveDevice(new DeviceId(MOCK_DEVICE_ID_3, DeviceType.MOCK_SHIFTING_VALUE, 5));
    }

}
