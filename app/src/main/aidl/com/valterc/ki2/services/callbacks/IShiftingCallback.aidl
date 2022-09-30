// IShiftingCallback.aidl
package com.valterc.ki2.services.callbacks;

import com.valterc.ki2.data.device.DeviceId;
import com.valterc.ki2.data.shifting.ShiftingInfo;

interface IShiftingCallback {

    void onShifting(in DeviceId deviceId, in ShiftingInfo shiftingInfo);
}