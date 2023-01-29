package com.valterc.ki2.ant.scanner;

import com.dsi.ant.channel.IAntChannelEventHandler;
import com.dsi.ant.message.ChannelId;
import com.dsi.ant.message.ExtendedData;
import com.dsi.ant.message.fromant.BroadcastDataMessage;
import com.dsi.ant.message.fromant.MessageFromAntType;
import com.dsi.ant.message.ipc.AntMessageParcel;
import com.valterc.ki2.ant.AntManager;
import com.valterc.ki2.ant.channel.AntChannelWrapper;
import com.valterc.ki2.ant.channel.ScanChannelConfiguration;
import com.valterc.ki2.data.device.DeviceId;

import timber.log.Timber;

public class AntScanner {

    private final AntManager antManager;
    private final IAntScanListener scanListener;
    private AntChannelWrapper antChannelWrapper;

    public AntScanner(AntManager antManager, IAntScanListener scanListener) {
        this.antManager = antManager;
        this.scanListener = scanListener;
    }

    public void startScan(ScanChannelConfiguration scanChannelConfiguration) throws Exception {
        if (antChannelWrapper != null) {
            return;
        }

        antChannelWrapper = antManager.getScanAntChannel(scanChannelConfiguration, new IAntChannelEventHandler() {
            @Override
            public void onReceiveMessage(MessageFromAntType messageFromAntType, AntMessageParcel antMessageParcel) {
                DeviceId deviceId = processScanResult(messageFromAntType, antMessageParcel);
                if (deviceId != null) {
                    scanListener.onAntScanResult(deviceId);
                }
            }

            @Override
            public void onChannelDeath() {
                stopScan();

                try {
                    if (antManager.isReady()) {
                        startScan(scanChannelConfiguration);
                    }
                } catch (Exception e) {
                    Timber.w(e, "Unable to restart scan");
                }
            }
        }, null);
    }

    public void stopScan() {
        AntChannelWrapper antChannelWrapper = this.antChannelWrapper;
        this.antChannelWrapper = null;

        if (antChannelWrapper != null) {
            antChannelWrapper.dispose();
        }
    }

    public final DeviceId processScanResult(MessageFromAntType messageFromAntType, AntMessageParcel antMessageParcel) {
        if (antMessageParcel == null || messageFromAntType != MessageFromAntType.BROADCAST_DATA) {
            return null;
        }

        BroadcastDataMessage broadcastDataMessage = new BroadcastDataMessage(antMessageParcel);
        if (!broadcastDataMessage.hasExtendedData()) {
            return null;
        }

        ExtendedData extendedData = broadcastDataMessage.getExtendedData();
        if (extendedData == null) {
            return null;
        }

        ChannelId channelId = extendedData.getChannelId();
        if (channelId == null) {
            return null;
        }

        return new DeviceId(channelId.getDeviceNumber(), channelId.getDeviceType(), channelId.getTransmissionType());
    }

}
