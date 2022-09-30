package com.valterc.ki2.ant.channel;

import com.dsi.ant.channel.NetworkKey;
import com.dsi.ant.message.ChannelId;
import com.dsi.ant.message.ChannelType;
import com.dsi.ant.message.LowPrioritySearchTimeout;

public class ChannelConfiguration {

    private final ChannelId channelId;
    private final ChannelType channelType;
    private final LowPrioritySearchTimeout lowPrioritySearchTimeout;
    private final NetworkKey networkKey;
    private final int period;
    private final int rfFrequency;
    private final Integer searchPriority;

    public ChannelConfiguration(ChannelId channelId, ChannelType channelType, LowPrioritySearchTimeout lowPrioritySearchTimeout, NetworkKey networkKey, int period, int rfFrequency, Integer searchPriority) {
        this.channelId = channelId;
        this.channelType = channelType;
        this.lowPrioritySearchTimeout = lowPrioritySearchTimeout;
        this.networkKey = networkKey;
        this.period = period;
        this.rfFrequency = rfFrequency;
        this.searchPriority = searchPriority;
    }

    public ChannelId getChannelId() {
        return channelId;
    }

    public ChannelType getChannelType() {
        return channelType;
    }

    public LowPrioritySearchTimeout getLowPrioritySearchTimeout() {
        return lowPrioritySearchTimeout;
    }

    public NetworkKey getNetworkKey() {
        return networkKey;
    }

    public int getPeriod() {
        return period;
    }

    public int getRfFrequency() {
        return rfFrequency;
    }

    public Integer getSearchPriority() {
        return searchPriority;
    }
}
