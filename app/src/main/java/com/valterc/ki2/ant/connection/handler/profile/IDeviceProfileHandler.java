package com.valterc.ki2.ant.connection.handler.profile;

import com.dsi.ant.message.Rssi;
import com.dsi.ant.message.fromant.AcknowledgedDataMessage;
import com.dsi.ant.message.fromant.BroadcastDataMessage;
import com.valterc.ki2.data.command.CommandType;

import java.util.Collection;

public interface IDeviceProfileHandler {

    byte[] getAcknowledgedData();

    byte[] getBroadcastData();

    void onRssi(Rssi rssi);

    void onAcknowledgedData(AcknowledgedDataMessage acknowledgedDataMessage);

    void onBroadcastData(BroadcastDataMessage broadcastDataMessage);

    Collection<CommandType> getSupportedCommands();

    void sendCommand(CommandType commandType, Object data);

}
