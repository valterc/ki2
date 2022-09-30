package com.valterc.ki2.ant.connection.handler.profile;

import com.valterc.ki2.ant.connection.IDeviceConnectionListener;
import com.valterc.ki2.ant.connection.handler.transport.ITransportHandler;
import com.valterc.ki2.data.device.DeviceId;

import timber.log.Timber;

public final class ProfileHandlerFactory {

    private ProfileHandlerFactory() {
    }

    public static IDeviceProfileHandler getProfileHandler(DeviceId deviceId, ITransportHandler transportHandler, IDeviceConnectionListener deviceConnectionListener) {

        if (deviceId == null || deviceId.getDeviceType() == null) {
            return null;
        }

        switch (deviceId.getDeviceType()) {
            case SHIMANO_SHIFTING:
                return new ShimanoShiftingProfileHandler(deviceId, transportHandler, deviceConnectionListener);
        }

        Timber.w("Unable to construct connection handler for device type %s", deviceId.getDeviceType());
        return null;
    }

}
