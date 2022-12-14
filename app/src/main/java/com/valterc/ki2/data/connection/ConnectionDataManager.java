package com.valterc.ki2.data.connection;

import android.os.Parcelable;

import com.valterc.ki2.data.device.DeviceId;
import com.valterc.ki2.data.info.DataInfo;
import com.valterc.ki2.data.info.DataInfoBuilder;
import com.valterc.ki2.data.info.DataType;

import java.util.HashMap;
import java.util.Iterator;
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
        Map<DataType, DataInfo> map = new HashMap<>();
        for (DataInfoBuilder dataInfoBuilder : dataMap.values()) {
            map.put(dataInfoBuilder.getType(), dataInfoBuilder.buildDataInfo());
        }
        return new ConnectionDataInfo(deviceId, status, map);
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

    public void clearEvents() {
        Iterator<Map.Entry<DataType, DataInfoBuilder>> iterator = dataMap.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<DataType, DataInfoBuilder> next = iterator.next();
            if (next.getKey().isEvent()) {
                iterator.remove();
            }
        }
    }
}
