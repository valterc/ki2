package com.valterc.ki2.karoo.service;

import com.valterc.ki2.data.connection.ConnectionInfo;
import com.valterc.ki2.data.connection.ConnectionStatus;
import com.valterc.ki2.data.device.DeviceId;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

public class ConnectionFilter {

    private final Map<DeviceId, ConnectionStatus> deviceIdConnectionStatusMap;
    private DeviceId deviceIdConnected;
    private boolean emitEstablishedEvent;

    public ConnectionFilter() {
        this.deviceIdConnectionStatusMap = new HashMap<>();
    }

    public boolean onConnectionStatusReceived(DeviceId deviceId, ConnectionStatus connectionStatus) {
        deviceIdConnectionStatusMap.put(deviceId, connectionStatus);

        if (deviceIdConnected == null && connectionStatus == ConnectionStatus.ESTABLISHED) {
            deviceIdConnected = deviceId;
            return true;
        } else if (deviceIdConnected == null) {
            return true;
        } else if (deviceIdConnected.equals(deviceId)) {
            if (connectionStatus != ConnectionStatus.CLOSED &&
                    connectionStatus != ConnectionStatus.INVALID) {
                return true;
            }

            deviceIdConnected = null;

            for (DeviceId id : deviceIdConnectionStatusMap.keySet()) {
                if (deviceIdConnectionStatusMap.get(id) == ConnectionStatus.ESTABLISHED) {
                    deviceIdConnected = id;
                    emitEstablishedEvent = true;
                    break;
                }
            }

            return true;
        }

        return false;
    }

    public boolean shouldEmitEstablishedEvent() {
        return emitEstablishedEvent;
    }

    public void emitEstablishedEvent(BiConsumer<DeviceId, ConnectionInfo> consumer) {
        emitEstablishedEvent = false;
        consumer.accept(deviceIdConnected, new ConnectionInfo(deviceIdConnectionStatusMap.get(deviceIdConnected)));
    }

    public boolean onDataReceived(DeviceId deviceId) {
        return deviceIdConnected == null || deviceIdConnected.equals(deviceId);
    }

    public void reset() {
        deviceIdConnected = null;
        deviceIdConnectionStatusMap.clear();
    }

}
