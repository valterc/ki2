// IManufacturerInfoCallback.aidl
package com.valterc.ki2.services.callbacks;

import com.valterc.ki2.data.device.DeviceId;
import com.valterc.ki2.data.info.ManufacturerInfo;

interface IManufacturerInfoCallback {

    void onManufacturerInfo(in DeviceId deviceId, in ManufacturerInfo manufacturerInfo);
}