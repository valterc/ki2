package com.valterc.ki2.data.configuration;

import android.content.Context;
import android.util.Base64;

import com.dsi.ant.channel.NetworkKey;
import com.dsi.ant.message.ChannelId;
import com.dsi.ant.message.ChannelType;
import com.dsi.ant.message.LowPrioritySearchTimeout;
import com.valterc.ki2.R;
import com.valterc.ki2.ant.channel.ChannelConfiguration;
import com.valterc.ki2.ant.channel.ScanChannelConfiguration;

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
        byte[] networkKey = Base64.decode(context.getString(R.string.network_key), Base64.DEFAULT);
        return new ScanChannelConfiguration(new NetworkKey(networkKey), 8198, 57);
    }

    /**
     * Get channel configuration.
     *
     * @param context Context of the caller.
     * @param antDeviceId ANT device id.
     * @return Channel configuration.
     */
    public static ChannelConfiguration getChannelConfiguration(Context context, int antDeviceId) {
        byte[] networkKey = Base64.decode(context.getString(R.string.network_key), Base64.DEFAULT);
        return new ChannelConfiguration(
                new ChannelId(antDeviceId, 1, 5),
                ChannelType.BIDIRECTIONAL_SLAVE,
                LowPrioritySearchTimeout.FIVE_SECONDS,
                new NetworkKey(networkKey),
                8198,
                57,
                9);
    }

}
