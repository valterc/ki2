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
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import timber.log.Timber;

public class AntDeviceConnection implements IAntDeviceConnection, IDeviceConnectionListener {

    private static final int MAX_RECONNECT_ATTEMPTS = 15;
    private static final int TIME_MS_MESSAGE_TIMEOUT = 30_000;
    private static final int TIME_S_CONNECTION_TRACKER_INTERVAL = 30;

    private final AntManager antManager;
    private final DeviceId deviceId;
    private final ChannelConfiguration channelConfiguration;
    private final IDeviceConnectionListener deviceConnectionListener;
    private final Queue<Pair<MessageFromAntType, AntMessageParcel>> messageQueue;
    private final ScheduledExecutorService executorService;

    private ConnectionStatus connectionStatus;
    private AntChannelWrapper antChannelWrapper;
    private ITransportHandler transportHandler;
    private long timestampLastMessage;
    private int reconnectAttempts;
    private boolean disconnected;

    public AntDeviceConnection(AntManager antManager, DeviceId deviceId, ChannelConfiguration channelConfiguration, IDeviceConnectionListener deviceConnectionListener) {
        this.antManager = antManager;
        this.deviceId = deviceId;
        this.channelConfiguration = channelConfiguration;
        this.deviceConnectionListener = deviceConnectionListener;
        this.messageQueue = new LinkedList<>();
        this.executorService = Executors.newSingleThreadScheduledExecutor();

        postConnectionStatus(deviceId, ConnectionStatus.NEW);
        executorService.schedule(this::connectionTracker, TIME_S_CONNECTION_TRACKER_INTERVAL * 2, TimeUnit.SECONDS);
        connect();
    }

    private void connectInternal() throws Exception {
        antChannelWrapper = antManager.getAntChannel(channelConfiguration, new IAntChannelEventHandler() {
            @Override
            public void onReceiveMessage(MessageFromAntType messageFromAntType, AntMessageParcel antMessageParcel) {
                if (disconnected) {
                    disconnectInternal();
                    return;
                }

                timestampLastMessage = System.currentTimeMillis();
                forwardMessage(messageFromAntType, antMessageParcel);
            }

            @Override
            public void onChannelDeath() {
                Timber.w("[%s] Channel died", deviceId);
                disconnectInternal();
                connect();
            }
        }, null);

        transportHandler = new TransportHandler(deviceId, this, this);
        pushQueuedMessages();
    }

    private void attemptConnect() {
        if (disconnected || connectionStatus == ConnectionStatus.CLOSED) {
            return;
        }

        try {
            Timber.d("[%s] Starting connect procedure", deviceId);
            connectInternal();
        } catch (Exception e) {
            Timber.w(e, "[%s] Unable to start connect procedure", deviceId);

            if (reconnectAttempts < MAX_RECONNECT_ATTEMPTS) {
                reconnectAttempts++;
                if (!disconnected && !executorService.isShutdown()) {
                    Timber.d("[%s] Retrying connection, attempt %d...", deviceId, reconnectAttempts);
                    executorService.schedule(this::attemptConnect, 2, TimeUnit.SECONDS);
                }
            } else {
                postConnectionStatus(deviceId, ConnectionStatus.CLOSED);
            }
        }
    }

    private void connect() {
        if (disconnected || connectionStatus == ConnectionStatus.CLOSED) {
            return;
        }

        postConnectionStatus(deviceId, ConnectionStatus.CONNECTING);

        if (!disconnected && !executorService.isShutdown()) {
            executorService.execute(this::attemptConnect);
        }
    }

    private void connectionTracker() {
        if (disconnected || connectionStatus == ConnectionStatus.CLOSED) {
            return;
        }

        if (connectionStatus == ConnectionStatus.ESTABLISHED) {
            if (System.currentTimeMillis() - timestampLastMessage > TIME_MS_MESSAGE_TIMEOUT) {
                timestampLastMessage = System.currentTimeMillis();
                Timber.w("[%s] No ANT messages in last 30 seconds, restarting connection...", deviceId);
                disconnectInternal();
                connect();
            }
        }

        if (!disconnected && !executorService.isShutdown()) {
            executorService.schedule(this::connectionTracker, TIME_S_CONNECTION_TRACKER_INTERVAL, TimeUnit.SECONDS);
        }
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

    private void disconnectInternal() {
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
        disconnected = true;
        executorService.shutdownNow();
        disconnectInternal();
        reconnectAttempts = MAX_RECONNECT_ATTEMPTS;
        if (this.connectionStatus != ConnectionStatus.CLOSED) {
            postConnectionStatus(deviceId, ConnectionStatus.CLOSED);
        }
    }

    @Override
    public ConnectionStatus getConnectionStatus() {
        return connectionStatus;
    }

    @Override
    public void sendCommand(CommandType commandType, Parcelable data) {
        ITransportHandler transportHandler = this.transportHandler;
        if (transportHandler == null) {
            return;
        }

        IDeviceProfileHandler deviceProfileHandler = transportHandler.getDeviceProfileHandler();
        if (deviceProfileHandler == null) {
            return;
        }

        deviceProfileHandler.sendCommand(commandType, data);
    }

    public void sendAcknowledgedData(byte[] payload) throws RemoteException, AntCommandFailedException {
        AntChannelWrapper antChannelWrapper = this.antChannelWrapper;
        if (antChannelWrapper != null) {
            antChannelWrapper.sendAcknowledgedData(payload);
        }
    }

    public void setBroadcastData(byte[] payload) throws RemoteException {
        AntChannelWrapper antChannelWrapper = this.antChannelWrapper;
        if (antChannelWrapper != null) {
            antChannelWrapper.setBroadcastData(payload);
        }
    }

    @Override
    public void onConnectionStatus(DeviceId deviceId, ConnectionStatus connectionStatus) {
        if (connectionStatus == ConnectionStatus.ESTABLISHED) {
            reconnectAttempts = 0;
        }

        if (connectionStatus == ConnectionStatus.CLOSED) {
            disconnectInternal();
            if (reconnectAttempts < MAX_RECONNECT_ATTEMPTS) {
                reconnectAttempts++;
                Timber.d("[%s] Retrying connection to device, attempt %d...", deviceId, reconnectAttempts);
                connect();
                return;
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
