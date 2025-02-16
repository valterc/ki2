package com.valterc.ki2.karoo.battery

import com.valterc.ki2.data.device.BatteryInfo
import com.valterc.ki2.data.device.DeviceId

class BatteryAlertRecord(val deviceId: DeviceId,
                         var batteryInfo: BatteryInfo? = null,
                         var alert : BatteryAlertType? = null,
                         var alertedInRide: Boolean = false)