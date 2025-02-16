package com.valterc.ki2.karoo.battery

import com.valterc.ki2.R
import com.valterc.ki2.data.device.BatteryInfo
import com.valterc.ki2.data.device.DeviceId
import com.valterc.ki2.data.device.DeviceName
import com.valterc.ki2.data.preferences.PreferencesView
import com.valterc.ki2.karoo.Ki2ExtensionContext
import com.valterc.ki2.karoo.RideHandler
import io.hammerhead.karooext.models.InRideAlert
import io.hammerhead.karooext.models.RideState
import io.hammerhead.karooext.models.SystemNotification
import java.util.function.BiConsumer
import java.util.function.Consumer

class BatteryAlertHandler(extensionContext: Ki2ExtensionContext) : RideHandler(extensionContext) {

    private var batteryLevelLow: Int? = null
    private var batteryLevelCritical: Int? = null
    private val notificationMap = mutableMapOf<DeviceId, BatteryAlertRecord>()

    private val preferencesConsumer = Consumer<PreferencesView> { preferences ->
        batteryLevelLow = preferences.getBatteryLevelLow(extensionContext.context)
        batteryLevelCritical = preferences.getBatteryLevelCritical(extensionContext.context)

        notificationMap.values.forEach { batteryRecord ->
            checkBatteryAndNotify(batteryRecord)
        }
    }

    private val batteryConsumer = BiConsumer<DeviceId, BatteryInfo> { deviceId, batteryInfo ->
        notificationMap[deviceId]?.let { batteryRecord ->
            batteryRecord.batteryInfo = batteryInfo
        } ?: run {
            notificationMap[deviceId] = BatteryAlertRecord(deviceId, batteryInfo)
        }

        notificationMap[deviceId]?.let { batteryRecord ->
            checkBatteryAndNotify(batteryRecord)
        }
    }

    init {
        extensionContext.serviceClient.registerUnfilteredBatteryInfoWeakListener(batteryConsumer)
        extensionContext.serviceClient.registerPreferencesWeakListener(preferencesConsumer)
    }

    override fun onRideStart() {
        notificationMap.values.forEach { batteryRecord ->
            checkBatteryAndNotify(batteryRecord)
        }
    }

    override fun onRideResume() {
        notificationMap.values.forEach { batteryRecord ->
            checkBatteryAndNotify(batteryRecord)
        }
    }

    override fun onRideEnd() {
        notificationMap.values.forEach { batteryRecord ->
            batteryRecord.alertedInRide = false
        }
    }

    private fun checkBatteryAndNotify(batteryAlertRecord: BatteryAlertRecord) {
        if (extensionContext.karooDeviceTracking.isDeviceConnected(batteryAlertRecord.deviceId)) {
            // If the device was added to Karoo, let Karoo handle the battery notification
            return
        }

        batteryAlertRecord.batteryInfo?.let { batteryInfo ->
            val batteryLevelCritical = batteryLevelCritical
            val batteryLevelLow = batteryLevelLow

            if (batteryLevelCritical != null && batteryInfo.value <= batteryLevelCritical) {
                notify(batteryAlertRecord, batteryInfo, BatteryAlertType.Critical)
            } else if (batteryLevelLow != null && batteryInfo.value <= batteryLevelLow) {
                notify(batteryAlertRecord, batteryInfo, BatteryAlertType.Low)
            }
        }
    }

    private fun notify(
        batteryAlertRecord: BatteryAlertRecord,
        batteryInfo: BatteryInfo,
        batteryAlertType: BatteryAlertType
    ) {
        if (batteryAlertRecord.alert == batteryAlertType && batteryAlertRecord.alertedInRide) {
            return
        }

        val devicePreferences =
            extensionContext.serviceClient.getDevicePreferences(batteryAlertRecord.deviceId)
        val deviceName =
            devicePreferences?.getName(extensionContext.context) ?: DeviceName.getDefaultName(
                extensionContext.context,
                batteryAlertRecord.deviceId
            )
        val title = extensionContext.context.getString(R.string.text_di2_low_battery)
        val detail = extensionContext.context.getString(
            R.string.text_param_low_battery,
            deviceName,
            batteryInfo.value
        )

        if (!batteryAlertRecord.alertedInRide && rideState is RideState.Recording) {
            batteryAlertRecord.alertedInRide = true
            extensionContext.karooSystem.dispatch(
                InRideAlert(
                    "ki2-ride-alert-battery-${batteryAlertRecord.deviceId.uid}",
                    R.drawable.ic_hh_battery,
                    title,
                    detail,
                    12_000,
                    backgroundColor = R.color.hh_red_600,
                    textColor = R.color.white
                )
            )
            extensionContext.audioManager.playKarooDeviceWarning()
        }

        extensionContext.karooSystem.dispatch(
            SystemNotification(
                "ki2-notification-battery-${batteryAlertRecord.deviceId.uid}",
                detail,
                header = title,
                style = if (batteryAlertType == BatteryAlertType.Critical) SystemNotification.Style.ERROR else SystemNotification.Style.EVENT
            )
        )

        batteryAlertRecord.alert = batteryAlertType
    }

}