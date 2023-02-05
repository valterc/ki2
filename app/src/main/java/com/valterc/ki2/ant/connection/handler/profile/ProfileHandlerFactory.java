package com.valterc.ki2.ant.connection.handler.profile;

import com.valterc.ki2.ant.connection.IDeviceConnectionListener;
import com.valterc.ki2.ant.connection.handler.transport.ITransportHandler;
import com.valterc.ki2.data.device.DeviceId;
import com.valterc.ki2.data.device.DeviceType;

import timber.log.Timber;

public final class ProfileHandlerFactory {

    private ProfileHandlerFactory() {
    }

    public static IDeviceProfileHandler buildProfileHandler(DeviceId deviceId, ITransportHandler transportHandler, IDeviceConnectionListener deviceConnectionListener) {
        if (deviceId == null || deviceId.getDeviceType() == null) {
            throw new IllegalArgumentException("Device identifier is invalid: " + deviceId);
        }

        switch (deviceId.getDeviceType()) {
            case SHIMANO_SHIFTING:
                return new ShimanoShiftingProfileHandler(deviceId, transportHandler, deviceConnectionListener);

            case SHIMANO_EBIKE:
                return new ShimanoEBikeProfileHandler(deviceId, transportHandler, deviceConnectionListener);
        }

        Timber.e("Unable to construct connection handler for device %s", deviceId);
        throw new RuntimeException("Unable to construct connection handler for device: " + deviceId);
    }

}
