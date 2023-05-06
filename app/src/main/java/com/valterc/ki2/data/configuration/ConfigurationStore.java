package com.valterc.ki2.data.configuration;

import android.content.Context;

import com.dsi.ant.channel.NetworkKey;
import com.dsi.ant.message.ChannelId;
import com.dsi.ant.message.ChannelType;
import com.dsi.ant.message.LowPrioritySearchTimeout;
import com.valterc.ki2.R;
import com.valterc.ki2.ant.channel.ChannelConfiguration;
import com.valterc.ki2.ant.channel.ScanChannelConfiguration;
import com.valterc.ki2.data.device.DeviceId;

import java.util.Base64;

public final class ConfigurationStore {

    private ConfigurationStore() {
    }

    /**
     * Get scan channel configuration.
     *
     * @param context Context of the caller.
     * @return Scan channel configuration.
     */
    public static ScanChannelConfiguration getScanChannelConfiguration(Context context) {
        byte[] networkKey = Base64.getDecoder().decode(context.getString(R.string.network_key));
        return new ScanChannelConfiguration(new NetworkKey(networkKey), 8198, 57);
    }

    /**
     * Get channel configuration.
     *
     * @param context  Context of the caller.
     * @param deviceId Device identifier.
     * @return Channel configuration.
     */
    public static ChannelConfiguration getChannelConfiguration(Context context, DeviceId deviceId) {
        byte[] networkKey = Base64.getDecoder().decode(context.getString(R.string.network_key));
        return new ChannelConfiguration(
                new ChannelId(deviceId.getDeviceNumber(), deviceId.getDeviceTypeValue(), deviceId.getTransmissionType()),
                ChannelType.BIDIRECTIONAL_SLAVE,
                LowPrioritySearchTimeout.TEN_SECONDS,
                new NetworkKey(networkKey),
                8198,
                57,
                9);
    }

}
