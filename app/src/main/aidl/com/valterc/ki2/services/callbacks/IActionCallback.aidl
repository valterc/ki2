// IActionCallback.aidl
package com.valterc.ki2.services.callbacks;

import com.valterc.ki2.data.device.DeviceId;
import com.valterc.ki2.data.action.KarooActionEvent;

interface IActionCallback {

    void onActionEvent(in DeviceId deviceId, in KarooActionEvent actionEvent);

}