package com.valterc.ki2.ant.scanner;

import android.os.Handler;
import android.os.Looper;

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

    private static final int TIME_MS_ATTEMPT_START_SCAN = 2000;

    private final AntManager antManager;
    private final IAntScanListener scanListener;
    private final Handler handler;
    private AntChannelWrapper antChannelWrapper;
    private Boolean scanEnabled;

    public AntScanner(AntManager antManager, IAntScanListener scanListener) {
        this.antManager = antManager;
        this.scanListener = scanListener;
        this.handler = new Handler(Looper.getMainLooper());
    }

    public void startScan(ScanChannelConfiguration scanChannelConfiguration) {
        if (antChannelWrapper != null) {
            return;
        }

        scanEnabled = true;
        handler.post(() -> starScanInternal(scanChannelConfiguration));
    }

    private void starScanInternal(ScanChannelConfiguration scanChannelConfiguration) {
        if (!scanEnabled || antChannelWrapper != null) {
            return;
        }

        try {
            getScanAntChannel(scanChannelConfiguration);
            Timber.i("Scan started");
        } catch (Exception e) {
            Timber.w(e, "Unable to start scan");
            handler.postDelayed(() -> starScanInternal(scanChannelConfiguration), (int) (TIME_MS_ATTEMPT_START_SCAN * (1 + 2 * Math.random())));
        }
    }

    private void getScanAntChannel(ScanChannelConfiguration scanChannelConfiguration) throws Exception {
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
                    if (antManager.isAntServiceReady()) {
                        startScan(scanChannelConfiguration);
                    }
                } catch (Exception e) {
                    Timber.w(e, "Unable to restart scan");
                }
            }
        }, null);
    }

    public void stopScan() {
        scanEnabled = false;
        handler.post(this::stopScanInternal);
    }

    private void stopScanInternal() {
        scanEnabled = false;

        AntChannelWrapper antChannelWrapper = this.antChannelWrapper;
        this.antChannelWrapper = null;

        if (antChannelWrapper != null) {
            antChannelWrapper.dispose();
            Timber.i("Scan stopped");
        }
    }

    private DeviceId processScanResult(MessageFromAntType messageFromAntType, AntMessageParcel antMessageParcel) {
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
