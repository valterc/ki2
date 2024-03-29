package com.valterc.ki2.ant.connection;

import android.content.Context;

import com.valterc.ki2.ant.AntManager;
import com.valterc.ki2.ant.channel.ChannelConfiguration;
import com.valterc.ki2.ant.connection.mock.MockShiftingDeviceConnection;
import com.valterc.ki2.data.configuration.ConfigurationStore;
import com.valterc.ki2.data.device.DeviceId;

import timber.log.Timber;

public final class DeviceConnectionFactory {

    private DeviceConnectionFactory() {
    }

    public static IAntDeviceConnection buildDeviceConnection(Context context,
                                                             AntManager antManager,
                                                             DeviceId deviceId,
                                                             IDeviceConnectionListener deviceConnectionListener) {
        if (deviceId == null || deviceId.getDeviceType() == null) {
            throw new IllegalArgumentException("Device identifier is invalid: " + deviceId);
        }

        switch (deviceId.getDeviceType()) {
            case SHIMANO_SHIFTING:
            case SHIMANO_EBIKE:
                ChannelConfiguration channelConfiguration = ConfigurationStore.getChannelConfiguration(context, deviceId);
                return new AntDeviceConnection(antManager, deviceId, channelConfiguration, deviceConnectionListener);

            case MOCK_SHIFTING:
                return new MockShiftingDeviceConnection(deviceId, deviceConnectionListener);
        }

        Timber.e("Unable to construct connection for device %s", deviceId);
        throw new RuntimeException("Unable to construct connection for device: " + deviceId);
    }

}
