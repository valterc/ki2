package com.valterc.ki2.data.connection;

import android.os.Parcelable;

import com.valterc.ki2.data.device.DeviceId;
import com.valterc.ki2.data.info.DataType;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class ConnectionsDataManager {

    private Map<DeviceId, ConnectionDataManager> connectionDataManagersMap;

    public ConnectionsDataManager() {
        connectionDataManagersMap = new HashMap<>();
    }

    public void setConnections(Collection<DeviceId> devices){
        connectionDataManagersMap.keySet().removeIf(deviceId -> !devices.contains(deviceId));
        for (DeviceId deviceId: devices) {
            connectionDataManagersMap.computeIfAbsent(deviceId, (d) -> new ConnectionDataManager(d));
        }
    }

    public void addConnection(DeviceId deviceId){
        connectionDataManagersMap.put(deviceId, new ConnectionDataManager(deviceId));
    }

    public void removeConnection(DeviceId deviceId){
        connectionDataManagersMap.remove(deviceId);
    }

    public boolean onConnectionStatus(DeviceId deviceId, ConnectionStatus connectionStatus){
        ConnectionDataManager connectionDataManager = connectionDataManagersMap.get(deviceId);
        if (connectionDataManager != null)
        {
            return connectionDataManager.onConnectionStatus(connectionStatus);
        }

        return false;
    }

    public boolean onData(DeviceId deviceId, DataType dataType, Parcelable data) {
        ConnectionDataManager connectionDataManager = connectionDataManagersMap.get(deviceId);
        if (connectionDataManager != null)
        {
            return connectionDataManager.onData(dataType, data);
        }

        return false;
    }

    public ConnectionInfo buildConnectionInfo(DeviceId deviceId){
        ConnectionDataManager connectionDataManager = connectionDataManagersMap.get(deviceId);
        if (connectionDataManager != null)
        {
            return connectionDataManager.buildConnectionInfo();
        }

        return null;
    }

    public ConnectionDataInfo buildConnectionDataInfo(DeviceId deviceId){
        ConnectionDataManager connectionDataManager = connectionDataManagersMap.get(deviceId);
        if (connectionDataManager != null)
        {
            return connectionDataManager.buildConnectionDataInfo();
        }

        return null;
    }

    public Parcelable getData(DeviceId deviceId, DataType dataType) {
        ConnectionDataManager connectionDataManager = connectionDataManagersMap.get(deviceId);
        if (connectionDataManager != null)
        {
            return connectionDataManager.getData(dataType);
        }

        return null;
    }

    public Collection<ConnectionDataManager> getDataManagers(){
        return connectionDataManagersMap.values();
    }

}
