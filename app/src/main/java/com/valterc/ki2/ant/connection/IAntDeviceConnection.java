package com.valterc.ki2.ant.connection;

import android.os.Parcelable;

import com.valterc.ki2.data.command.CommandType;
import com.valterc.ki2.data.connection.ConnectionStatus;
import com.valterc.ki2.data.device.DeviceId;

public interface IAntDeviceConnection {

    DeviceId getDeviceId();

    /**
     * Connects to the device. If the connection is already established, this method does nothing.
     * If the connection was previously established and then forcefully disconnected, this method does nothing, the connection must be recreated.
     *
     * @return True if the connection procedure was started - this does not indicate that the connection was successful, false if it's not possible to start connection procedure.
     */
    boolean connect();

    /**
     * Disconnects from the device without sending any connection status events.
     * If the connection is already disconnected, this method does nothing.
     */
    void disconnectSilent();

    void disconnect();

    ConnectionStatus getConnectionStatus();

    void sendCommand(CommandType commandType, Parcelable data);

}
