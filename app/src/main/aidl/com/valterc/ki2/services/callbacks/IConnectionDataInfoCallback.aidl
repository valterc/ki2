// IConnectionDataInfoCallback.aidl
package com.valterc.ki2.services.callbacks;

import com.valterc.ki2.data.connection.ConnectionDataInfo;
import com.valterc.ki2.data.device.DeviceId;

interface IConnectionDataInfoCallback {

    void onConnectionDataInfo(in DeviceId deviceId, in ConnectionDataInfo connectionDataInfo);

}