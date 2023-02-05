package com.valterc.ki2.ant.connection.handler.profile;

import com.valterc.ki2.ant.connection.IDeviceConnectionListener;
import com.valterc.ki2.ant.connection.handler.transport.ITransportHandler;
import com.valterc.ki2.data.device.DeviceId;

public class ShimanoEBikeProfileHandler extends ShimanoShiftingProfileHandler {

    public ShimanoEBikeProfileHandler(DeviceId deviceId, ITransportHandler transportHandler, IDeviceConnectionListener deviceConnectionListener) {
        super(deviceId, transportHandler, deviceConnectionListener);
    }

}
