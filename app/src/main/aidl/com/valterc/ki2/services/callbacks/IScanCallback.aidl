// IScanCallback.aidl
package com.valterc.ki2.services.callbacks;

import com.valterc.ki2.data.device.DeviceId;

interface IScanCallback {

    void onScanResult(in DeviceId deviceId);

}