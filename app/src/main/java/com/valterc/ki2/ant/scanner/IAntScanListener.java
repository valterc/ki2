package com.valterc.ki2.ant.scanner;

import com.valterc.ki2.data.device.DeviceId;

public interface IAntScanListener {

    void onAntScanResult(DeviceId deviceId);

}
