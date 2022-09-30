package com.valterc.ki2.ant.channel;

import com.dsi.ant.channel.NetworkKey;

public class ScanChannelConfiguration {

    private final NetworkKey networkKey;
    private final Integer period;
    private final int rfFrequency;

    public ScanChannelConfiguration(NetworkKey networkKey, Integer period, int rfFrequency) {
        this.networkKey = networkKey;
        this.period = period;
        this.rfFrequency = rfFrequency;
    }

    public NetworkKey getNetworkKey() {
        return networkKey;
    }

    public Integer getPeriod() {
        return period;
    }

    public int getRfFrequency() {
        return rfFrequency;
    }
}
