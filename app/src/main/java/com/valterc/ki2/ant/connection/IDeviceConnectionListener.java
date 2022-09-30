package com.valterc.ki2.ant.connection;

import com.valterc.ki2.data.connection.ConnectionStatus;
import com.valterc.ki2.data.device.DeviceId;
import com.valterc.ki2.data.info.DataType;

public interface IDeviceConnectionListener {

    void onConnectionStatus(DeviceId deviceId, ConnectionStatus connectionStatus);

    void onData(DeviceId deviceId, DataType dataType, Object data);

}
