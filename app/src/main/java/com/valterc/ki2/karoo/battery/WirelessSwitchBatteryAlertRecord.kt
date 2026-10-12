package com.valterc.ki2.karoo.battery

import com.valterc.ki2.data.device.DeviceId
import com.valterc.ki2.data.switches.WirelessSwitchInfo

data class WirelessSwitchBatteryAlertKey(val deviceId: DeviceId,
                                         val slot: Int)

class WirelessSwitchBatteryAlertRecord(val deviceId: DeviceId,
                                       var wirelessSwitchInfo: WirelessSwitchInfo,
                                       var notified: Boolean = false,
                                       var alertedInRide: Boolean = false)
