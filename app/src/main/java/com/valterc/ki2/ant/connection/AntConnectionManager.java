package com.valterc.ki2.ant.connection;

import android.content.Context;

import com.valterc.ki2.ant.AntManager;
import com.valterc.ki2.data.connection.ConnectionStatus;
import com.valterc.ki2.data.device.DeviceId;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.stream.Collectors;

import timber.log.Timber;

public class AntConnectionManager {

    private final Context context;
    private final AntManager antManager;
    private final Map<DeviceId, IAntDeviceConnection> connectionMap;

    public AntConnectionManager(Context context, AntManager antManager) {
        this.context = context;
        this.antManager = antManager;
        this.connectionMap = new HashMap<>();
    }

    public boolean isNoConnectionEstablished() {
        for (DeviceId deviceId : connectionMap.keySet()) {
            IAntDeviceConnection connection = connectionMap.get(deviceId);
            if (connection != null && connection.getConnectionStatus() == ConnectionStatus.ESTABLISHED) {
                return false;
            }
        }

        return true;
    }

    public void connectOnly(Collection<DeviceId> devices, IDeviceConnectionListener deviceConnectionListener) throws Exception {
        Collection<DeviceId> devicesToDisconnect = connectionMap.keySet()
                .stream()
                .filter(d -> !devices.contains(d))
                .collect(Collectors.toCollection(ArrayList::new));
        disconnect(devicesToDisconnect);

        connect(devices, deviceConnectionListener);
    }

    public void connect(Collection<DeviceId> devices, IDeviceConnectionListener deviceConnectionListener) throws Exception {
        int newDeviceCount = (int) devices.stream().filter(d -> !connectionMap.containsKey(d)).count();
        int availableAntChannels = antManager.getAvailableChannelCount();

        if (newDeviceCount > availableAntChannels) {
            throw new Exception("Not enough ANT channels available. Requested: " + newDeviceCount + ", available: " + availableAntChannels);
        }

        for (DeviceId deviceId : devices) {
            connect(deviceId, deviceConnectionListener, false);
        }
    }

    public void connect(DeviceId deviceId, IDeviceConnectionListener deviceConnectionListener, boolean forceReconnect) {
        IAntDeviceConnection existingConnection = connectionMap.get(deviceId);
        if (existingConnection != null) {
            if (!forceReconnect &&
                    (existingConnection.getConnectionStatus() == ConnectionStatus.ESTABLISHED ||
                            existingConnection.getConnectionStatus() == ConnectionStatus.CONNECTING ||
                            existingConnection.getConnectionStatus() == ConnectionStatus.CLOSED)) {
                return;
            }

            existingConnection.disconnect();
        }

        try {
            IAntDeviceConnection connection = DeviceConnectionFactory.buildDeviceConnection(context, antManager, deviceId, deviceConnectionListener);
            connectionMap.put(deviceId, connection);
        } catch (Exception e) {
            Timber.e(e, "Unable to create ANT connection for deviceId %s", deviceId);
        }
    }

    public void restartClosedConnections(IDeviceConnectionListener deviceConnectionListener) {
        for (DeviceId deviceId : connectionMap.keySet()) {
            IAntDeviceConnection existingConnection = connectionMap.get(deviceId);
            if (existingConnection == null ||
                    existingConnection.getConnectionStatus() != ConnectionStatus.ESTABLISHED) {
                connect(deviceId, deviceConnectionListener, true);
            }
        }
    }

    public void disconnect(DeviceId deviceId) {
        IAntDeviceConnection existingConnection = connectionMap.get(deviceId);
        if (existingConnection != null) {
            existingConnection.disconnect();
            connectionMap.remove(deviceId);
        }
    }

    public void disconnect(Collection<DeviceId> devices) {
        for (DeviceId deviceId : devices) {
            disconnect(deviceId);
        }
    }

    public void disconnectAll() {
        Iterator<DeviceId> iterator = connectionMap.keySet().iterator();

        while (iterator.hasNext()) {
            IAntDeviceConnection connection = connectionMap.get(iterator.next());
            if (connection != null) {
                connection.disconnect();
            }
            iterator.remove();
        }
    }

    public IAntDeviceConnection getConnection(DeviceId deviceId) {
        return connectionMap.get(deviceId);
    }

}
