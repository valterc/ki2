// ISwitchKeyCallback.aidl
package com.valterc.ki2.services.callbacks;

import com.valterc.ki2.data.device.DeviceId;
import com.valterc.ki2.data.switches.SwitchKeyEvent;

interface ISwitchKeyCallback {

    void onSwitchKeyEvent(in DeviceId deviceId, in SwitchKeyEvent switchKeyEvent);

}