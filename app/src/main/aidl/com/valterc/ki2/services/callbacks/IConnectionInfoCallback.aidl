// IConnectionInfoCallback.aidl
package com.valterc.ki2.services.callbacks;

import com.valterc.ki2.data.connection.ConnectionInfo;
import com.valterc.ki2.data.device.DeviceId;

interface IConnectionInfoCallback {

    void onConnectionInfo(in DeviceId deviceId, in ConnectionInfo connectionInfo);

}