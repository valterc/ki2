package com.valterc.ki2.ant.channel;

import android.os.RemoteException;

import com.dsi.ant.channel.AntChannel;
import com.dsi.ant.channel.AntCommandFailedException;

public class AntChannelWrapper {

    private final AntChannel antChannel;

    public AntChannelWrapper(AntChannel antChannel) throws Exception {
        this.antChannel = antChannel;
        antChannel.open();
    }

    public void sendAcknowledgedData(byte[] payload) throws RemoteException, AntCommandFailedException {
        this.antChannel.startSendAcknowledgedData(payload);
    }

    public void setBroadcastData(byte[] payload) throws RemoteException {
        this.antChannel.setBroadcastData(payload);
    }

    public void dispose() {
        try {
            antChannel.close();
        } catch (Exception e) {
            // ignore
        }

        try {
            antChannel.clearAdapterEventHandler();
            antChannel.clearChannelEventHandler();
        } catch (Exception e) {
            // ignore
        }

        try {
            antChannel.release();
        } catch (Exception e) {
            // ignore
        }
    }

}
