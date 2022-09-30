package com.valterc.ki2.ant.connection.handler.transport;

import com.dsi.ant.message.fromant.MessageFromAntType;
import com.dsi.ant.message.ipc.AntMessageParcel;
import com.valterc.ki2.ant.connection.handler.profile.IDeviceProfileHandler;

public interface ITransportHandler {
    void processAntMessage(MessageFromAntType messageFromAntType, AntMessageParcel antMessageParcel);

    IDeviceProfileHandler getDeviceProfileHandler();
}
