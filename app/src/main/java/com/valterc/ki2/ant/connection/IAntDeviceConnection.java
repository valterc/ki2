package com.valterc.ki2.ant.connection;

import android.os.Parcelable;

import com.valterc.ki2.data.command.CommandType;
import com.valterc.ki2.data.connection.ConnectionStatus;
import com.valterc.ki2.data.device.DeviceId;

public interface IAntDeviceConnection {

    DeviceId getDeviceId();

    void disconnect();

    ConnectionStatus getConnectionStatus();

    void sendCommand(CommandType commandType, Parcelable data);

}
