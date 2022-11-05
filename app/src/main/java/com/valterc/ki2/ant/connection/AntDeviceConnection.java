package com.valterc.ki2.ant.connection;

import android.os.Parcelable;
import android.os.RemoteException;
import android.util.Pair;

import com.dsi.ant.channel.AntCommandFailedException;
import com.dsi.ant.channel.IAntChannelEventHandler;
import com.dsi.ant.message.fromant.MessageFromAntType;
import com.dsi.ant.message.ipc.AntMessageParcel;
import com.valterc.ki2.ant.AntManager;
import com.valterc.ki2.ant.channel.AntChannelWrapper;
import com.valterc.ki2.ant.channel.ChannelConfiguration;
import com.valterc.ki2.ant.connection.handler.profile.IDeviceProfileHandler;
import com.valterc.ki2.ant.connection.handler.transport.ITransportHandler;
import com.valterc.ki2.ant.connection.handler.transport.TransportHandler;
import com.valterc.ki2.data.command.CommandType;
import com.valterc.ki2.data.connection.ConnectionStatus;
import com.valterc.ki2.data.device.DeviceId;
import com.valterc.ki2.data.info.DataType;

import java.util.LinkedList;
import java.util.Queue;

import timber.log.Timber;

public class AntDeviceConnection implements IAntDeviceConnection, IDeviceConnectionListener {

    private static final int MAX_RECONNECT_ATTEMPTS = 10;

    private final AntManager antManager;
    private final DeviceId deviceId;
    private final ChannelConfiguration channelConfiguration;
    private final IDeviceConnectionListener deviceConnectionListener;
    private final Queue<Pair<MessageFromAntType, AntMessageParcel>> messageQueue;

    private ConnectionStatus connectionStatus;
    private AntChannelWrapper antChannelWrapper;
    private ITransportHandler transportHandler;
    private int searchReconnectAttempts;

    public AntDeviceConnection(AntManager antManager, DeviceId deviceId, ChannelConfiguration channelConfiguration, IDeviceConnectionListener deviceConnectionListener) throws Exception {
        this.antManager = antManager;
        this.deviceId = deviceId;
        this.channelConfiguration = channelConfiguration;
        this.deviceConnectionListener = deviceConnectionListener;
        this.messageQueue = new LinkedList<>();
        onConnectionStatus(deviceId, ConnectionStatus.NEW);
        connect(antManager);
    }

    private void connect(AntManager antManager) throws Exception {
        antChannelWrapper = antManager.getAntChannel(channelConfiguration, new IAntChannelEventHandler() {
            @Override
            public void onReceiveMessage(MessageFromAntType messageFromAntType, AntMessageParcel antMessageParcel) {
                forwardMessage(messageFromAntType, antMessageParcel);
            }

            @Override
            public void onChannelDeath() {
                Timber.d("Channel died for device %s", deviceId);

                disconnect();

                if (antManager.isReady()) {
                    try {
                        connect(antManager);
                    } catch (Exception e) {
                        Timber.e(e, "Unable to restart connection for deviceId %s", deviceId);
                    }
                }
            }
        }, null);

        onConnectionStatus(deviceId, ConnectionStatus.CONNECTING);
        transportHandler = new TransportHandler(deviceId, this, this);
        pushQueuedMessages();
    }

    private void forwardMessage(MessageFromAntType messageFromAntType, AntMessageParcel antMessageParcel) {
        if (transportHandler == null) {
            messageQueue.add(new Pair<>(messageFromAntType, antMessageParcel));
        } else {
            pushQueuedMessages();
            transportHandler.processAntMessage(messageFromAntType, antMessageParcel);
        }
    }

    private synchronized void pushQueuedMessages() {
        ITransportHandler transportHandler = this.transportHandler;

        if (transportHandler == null) {
            return;
        }

        while (!messageQueue.isEmpty()) {
            Pair<MessageFromAntType, AntMessageParcel> queuedMessage = messageQueue.poll();
            if (queuedMessage != null) {
                transportHandler.processAntMessage(queuedMessage.first, queuedMessage.second);
            }
        }
    }

    private void disconnectInternal(){
        messageQueue.clear();
        AntChannelWrapper antChannelWrapper = this.antChannelWrapper;
        this.antChannelWrapper = null;
        this.transportHandler = null;

        if (antChannelWrapper != null) {
            antChannelWrapper.dispose();
        }
    }

    private void postConnectionStatus(DeviceId deviceId, ConnectionStatus connectionStatus) {
        this.connectionStatus = connectionStatus;
        if (deviceConnectionListener != null) {
            deviceConnectionListener.onConnectionStatus(deviceId, connectionStatus);
        }
    }

    @Override
    public DeviceId getDeviceId() {
        return deviceId;
    }

    @Override
    public void disconnect() {
        disconnectInternal();
        searchReconnectAttempts = MAX_RECONNECT_ATTEMPTS;
        if (this.connectionStatus != ConnectionStatus.CLOSED) {
            postConnectionStatus(deviceId, ConnectionStatus.CLOSED);
        }
    }

    @Override
    public ConnectionStatus getConnectionStatus(){
        return connectionStatus;
    }

    @Override
    public void sendCommand(CommandType commandType, Parcelable data) {
        ITransportHandler transportHandler = this.transportHandler;

        if (transportHandler == null){
            return;
        }

        IDeviceProfileHandler deviceProfileHandler = transportHandler.getDeviceProfileHandler();

        if (deviceProfileHandler == null){
            return;
        }

        deviceProfileHandler.sendCommand(commandType, data);
    }

    public void sendAcknowledgedData(byte[] payload) throws RemoteException, AntCommandFailedException {
        AntChannelWrapper antChannelWrapper =  this.antChannelWrapper;
        if (antChannelWrapper != null) {
            antChannelWrapper.sendAcknowledgedData(payload);
        }
    }

    public void setBroadcastData(byte[] payload) throws RemoteException {
        AntChannelWrapper antChannelWrapper =  this.antChannelWrapper;
        if (antChannelWrapper != null) {
            antChannelWrapper.setBroadcastData(payload);
        }
    }

    @Override
    public void onConnectionStatus(DeviceId deviceId, ConnectionStatus connectionStatus) {

        if (connectionStatus == ConnectionStatus.ESTABLISHED) {
            searchReconnectAttempts = 0;
        } else if (connectionStatus == ConnectionStatus.CLOSED && searchReconnectAttempts < MAX_RECONNECT_ATTEMPTS) {
            searchReconnectAttempts++;
            connectionStatus = ConnectionStatus.CONNECTING;

            try {
                Timber.d("[%s] Retrying connection, attempt %d...", deviceId, searchReconnectAttempts);
                disconnectInternal();
                connect(antManager);
            } catch (Exception e) {
                Timber.e(e, "Unable to connect");
                disconnectInternal();
                connectionStatus = ConnectionStatus.CLOSED;
            }
        }

        postConnectionStatus(deviceId, connectionStatus);
    }

    @Override
    public void onData(DeviceId deviceId, DataType dataType, Parcelable data) {
        if (deviceConnectionListener != null) {
            deviceConnectionListener.onData(deviceId, dataType, data);
        }
    }
}
