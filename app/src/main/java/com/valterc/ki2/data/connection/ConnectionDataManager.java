package com.valterc.ki2.data.connection;

import android.os.Parcelable;

import com.valterc.ki2.data.device.DeviceId;
import com.valterc.ki2.data.info.DataInfo;
import com.valterc.ki2.data.info.DataInfoBuilder;
import com.valterc.ki2.data.info.DataType;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class ConnectionDataManager {

    private final DeviceId deviceId;
    private final Map<DataType, DataInfoBuilder> dataMap;
    private ConnectionStatus status;

    public ConnectionDataManager(DeviceId deviceId) {
        this.deviceId = deviceId;
        dataMap = new HashMap<>();
        status = ConnectionStatus.INVALID;
    }

    public boolean onConnectionStatus(ConnectionStatus connectionStatus){
        if (connectionStatus != status)
        {
            status = connectionStatus;
            return true;
        }

        return false;
    }

    public boolean onData(DataType dataType, Parcelable data) {
        DataInfoBuilder dataInfoBuilder = dataMap.get(dataType);
        if (dataInfoBuilder == null) {
            dataMap.put(dataType, new DataInfoBuilder(dataType, data));
            return true;
        } else if (dataType.isEvent() || dataInfoBuilder.getValue() == null || !dataInfoBuilder.getValue().equals(data)) {
            dataInfoBuilder.setValue(data);
            return true;
        }

        return false;
    }

    public ConnectionInfo buildConnectionInfo(){
        return new ConnectionInfo(status);
    }

    public ConnectionDataInfo buildConnectionDataInfo(){
        return new ConnectionDataInfo(status, dataMap.values().stream().collect(Collectors.toMap(DataInfoBuilder::getType, DataInfoBuilder::buildDataInfo)));
    }

    public DataInfo buildDataInfo(DataType dataType) {
        DataInfoBuilder dataInfoBuilder = dataMap.get(dataType);
        if (dataInfoBuilder != null){
            return dataInfoBuilder.buildDataInfo();
        }

        return null;
    }

    public DeviceId getDeviceId() {
        return deviceId;
    }

    public ConnectionStatus getStatus() {
        return status;
    }

    public Parcelable getData(DataType dataType) {
        DataInfoBuilder dataInfoBuilder = dataMap.get(dataType);
        if (dataInfoBuilder != null){
            return dataInfoBuilder.getValue();
        }

        return null;
    }

}
