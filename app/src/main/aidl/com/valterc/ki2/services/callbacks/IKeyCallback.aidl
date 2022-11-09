// IKeyCallback.aidl
package com.valterc.ki2.services.callbacks;

import com.valterc.ki2.data.device.DeviceId;
import com.valterc.ki2.data.input.KarooKeyEvent;

interface IKeyCallback {

    void onKeyEvent(in DeviceId deviceId, in KarooKeyEvent keyEvent);

}