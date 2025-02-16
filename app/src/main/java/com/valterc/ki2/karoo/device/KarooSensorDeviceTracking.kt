package com.valterc.ki2.karoo.device

import com.valterc.ki2.data.device.DeviceId

class KarooSensorDeviceTracking {

    private val deviceMap = mutableMapOf<DeviceId, Boolean>()

    fun deviceConnect(uid: DeviceId) {
        deviceMap[uid] = true
    }

    fun isDeviceConnected(uid: DeviceId) : Boolean {
        return deviceMap[uid] == true
    }

}