package com.valterc.ki2.ant.connection.handler.transport;

import com.dsi.ant.message.ExtendedData;
import com.dsi.ant.message.ResponseCode;
import com.dsi.ant.message.Rssi;
import com.dsi.ant.message.fromant.AcknowledgedDataMessage;
import com.dsi.ant.message.fromant.BroadcastDataMessage;
import com.dsi.ant.message.fromant.ChannelEventMessage;
import com.dsi.ant.message.fromant.ChannelResponseMessage;
import com.dsi.ant.message.fromant.ChannelStatusMessage;
import com.dsi.ant.message.fromant.MessageFromAntType;
import com.dsi.ant.message.ipc.AntMessageParcel;
import com.valterc.ki2.ant.connection.AntDeviceConnection;
import com.valterc.ki2.ant.connection.IDeviceConnectionListener;
import com.valterc.ki2.ant.connection.handler.profile.IDeviceProfileHandler;
import com.valterc.ki2.ant.connection.handler.profile.ProfileHandlerFactory;
import com.valterc.ki2.data.connection.ConnectionStatus;
import com.valterc.ki2.data.device.DeviceId;

import java.util.Arrays;

import timber.log.Timber;

public class TransportHandler implements ITransportHandler {

    private final DeviceId deviceId;
    private final AntDeviceConnection antDeviceConnection;
    private final IDeviceConnectionListener deviceConnectionListener;
    private final IDeviceProfileHandler profileHandler;
    private boolean connectionEstablished;
    private boolean antBroadcasting;
    private byte[] acknowledgedDataSent;

    public TransportHandler(DeviceId deviceId, AntDeviceConnection antDeviceConnection, IDeviceConnectionListener deviceConnectionListener) {
        this.deviceId = deviceId;
        this.antDeviceConnection = antDeviceConnection;
        this.deviceConnectionListener = deviceConnectionListener;
        this.profileHandler = ProfileHandlerFactory.getProfileHandler(deviceId, this, deviceConnectionListener);
    }

    public final void processAntMessage(MessageFromAntType messageFromAntType, AntMessageParcel antMessageParcel) {
        if (messageFromAntType == null || antMessageParcel == null) {
            return;
        }

        switch (messageFromAntType) {
            case BROADCAST_DATA:
                BroadcastDataMessage broadcastDataMessage = new BroadcastDataMessage(antMessageParcel);
                if (!connectionEstablished) {
                    connectionEstablished = true;
                    deviceConnectionListener.onConnectionStatus(deviceId, ConnectionStatus.ESTABLISHED);
                }

                if (!this.antBroadcasting) {
                    setBroadcastData();
                }

                sendAcknowledgedData();

                ExtendedData extendedData = broadcastDataMessage.getExtendedData();
                if (extendedData.hasRssi()) {
                    Rssi rssi = extendedData.getRssi();
                    profileHandler.onRssi(rssi);
                }

                profileHandler.onBroadcastData(broadcastDataMessage);
                break;

            case ACKNOWLEDGED_DATA:
                profileHandler.onAcknowledgedData(new AcknowledgedDataMessage(antMessageParcel));
                break;

            case CHANNEL_EVENT:
                processAntEvent(new ChannelEventMessage(antMessageParcel));
                break;

            case CHANNEL_RESPONSE:
                ChannelResponseMessage channelResponseMessage = new ChannelResponseMessage(antMessageParcel);
                if (channelResponseMessage.getResponseCode() == ResponseCode.RESPONSE_NO_ERROR) {
                    return;
                }
                Timber.d("[%s] Channel response: %s", deviceId, channelResponseMessage);
                break;

            case CHANNEL_STATUS:
                ChannelStatusMessage channelStatusMessage = new ChannelStatusMessage(antMessageParcel);
                Timber.d("[%s] Channel status: %s", deviceId, channelStatusMessage);
                break;

            default:
                Timber.i("[%s] Unhandled message %s from ANT channel", deviceId, messageFromAntType);
        }
    }

    private void processAntEvent(ChannelEventMessage channelEventMessage) {
        Timber.d("Received ANT message %s: %s", channelEventMessage.getEventCode(), channelEventMessage);

        switch (channelEventMessage.getEventCode()) {
            case RX_SEARCH_TIMEOUT:
            case CHANNEL_CLOSED:
                this.connectionEstablished = false;
                this.antBroadcasting = false;
                this.acknowledgedDataSent = null;
                deviceConnectionListener.onConnectionStatus(deviceId, ConnectionStatus.CLOSED);
                break;

            case RX_FAIL:
            case CHANNEL_COLLISION:
                break;

            case TX:
                setBroadcastData();
                break;

            case TRANSFER_TX_COMPLETED:
                this.acknowledgedDataSent = null;
                break;

            case TRANSFER_TX_FAILED:
                resendAcknowledgedData();
                break;

            case RX_FAIL_GO_TO_SEARCH:
                this.connectionEstablished = false;
                this.antBroadcasting = false;
                this.acknowledgedDataSent = null;
                deviceConnectionListener.onConnectionStatus(deviceId, ConnectionStatus.CONNECTING);
                break;

            default:
                Timber.i("[%s] Unhandled ANT channel event %s", deviceId, channelEventMessage);
                break;
        }
    }

    private void setBroadcastData() {
        this.antBroadcasting = false;
        byte[] broadcastData = this.profileHandler.getBroadcastData();
        if (broadcastData != null) {
            try {
                antDeviceConnection.setBroadcastData(broadcastData);
                this.antBroadcasting = true;
            } catch (Exception e) {
                Timber.e(e, "[%s] Unable to set broadcast data", deviceId);
            }
        }
    }

    public void resendAcknowledgedData() {
        byte[] acknowledgedData = this.acknowledgedDataSent;
        if (acknowledgedData != null) {
            try {
                this.antDeviceConnection.sendAcknowledgedData(acknowledgedData);
            } catch (Exception e) {
                Timber.e(e, "[%s] Unable to send acknowledged data", deviceId);
            }
        }
    }

    public void sendAcknowledgedData() {
        byte[] acknowledgedData = this.profileHandler.getAcknowledgedData();
        if (this.acknowledgedDataSent == null && acknowledgedData != null) {
            try {
                this.antDeviceConnection.sendAcknowledgedData(acknowledgedData);
            } catch (Exception e) {
                Timber.e(e, "[%s] Unable to send acknowledged data", deviceId);
            }
            this.acknowledgedDataSent = Arrays.copyOf(acknowledgedData, acknowledgedData.length);
        }
    }

    public IDeviceProfileHandler getDeviceProfileHandler() {
        return profileHandler;
    }

}
