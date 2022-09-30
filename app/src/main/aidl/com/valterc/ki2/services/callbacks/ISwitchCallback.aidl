// ISwitchCallback.aidl
package com.valterc.ki2.services.callbacks;

import com.valterc.ki2.data.device.DeviceId;
import com.valterc.ki2.data.switches.SwitchEvent;

interface ISwitchCallback {

    void onSwitchEvent(in DeviceId deviceId, in SwitchEvent switchEvent);

}