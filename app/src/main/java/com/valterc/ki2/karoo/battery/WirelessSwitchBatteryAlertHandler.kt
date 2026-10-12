package com.valterc.ki2.karoo.battery

import android.os.Handler
import android.os.Looper
import com.valterc.ki2.R
import com.valterc.ki2.data.device.DeviceId
import com.valterc.ki2.data.preferences.PreferencesView
import com.valterc.ki2.data.switches.WirelessSwitchBatteryLevel
import com.valterc.ki2.data.switches.WirelessSwitchName
import com.valterc.ki2.data.switches.WirelessSwitchesInfo
import com.valterc.ki2.karoo.Ki2ExtensionContext
import com.valterc.ki2.karoo.RideHandler
import io.hammerhead.karooext.models.InRideAlert
import io.hammerhead.karooext.models.RideState
import io.hammerhead.karooext.models.SystemNotification
import java.util.function.BiConsumer
import java.util.function.Consumer

class WirelessSwitchBatteryAlertHandler(extensionContext: Ki2ExtensionContext) : RideHandler(extensionContext) {

    companion object {
        private const val ALERT_DURATION_MS = 12_000L
        private const val ALERT_GAP_MS = 1_000L
    }

    private var alertsEnabled = false
    private val alertMap = mutableMapOf<WirelessSwitchBatteryAlertKey, WirelessSwitchBatteryAlertRecord>()

    private val handler = Handler(Looper.getMainLooper())
    private val alertQueue = ArrayDeque<WirelessSwitchBatteryAlertRecord>()
    private var alertShowing = false
    private val showNextAlertRunnable = Runnable { showNextAlert() }

    private val preferencesConsumer = Consumer<PreferencesView> { preferences ->
        alertsEnabled = preferences.isWirelessSwitchBatteryNotificationEnabled(extensionContext.context)

        checkBatteriesAndNotify()
    }

    private val wirelessSwitchesConsumer = BiConsumer<DeviceId, WirelessSwitchesInfo> { deviceId, wirelessSwitchesInfo ->
        val slots = wirelessSwitchesInfo.switches.map { it.slot }.toSet()
        alertMap.keys.removeIf { key -> key.deviceId == deviceId && key.slot !in slots }

        wirelessSwitchesInfo.switches.forEach { wirelessSwitchInfo ->
            val alertRecord = alertMap.getOrPut(WirelessSwitchBatteryAlertKey(deviceId, wirelessSwitchInfo.slot)) {
                WirelessSwitchBatteryAlertRecord(deviceId, wirelessSwitchInfo)
            }
            alertRecord.wirelessSwitchInfo = wirelessSwitchInfo

            if (wirelessSwitchInfo.batteryLevel == WirelessSwitchBatteryLevel.MID ||
                wirelessSwitchInfo.batteryLevel == WirelessSwitchBatteryLevel.HIGH) {
                alertRecord.notified = false
            }
        }

        checkBatteriesAndNotify()
    }

    init {
        extensionContext.serviceClient.registerUnfilteredWirelessSwitchesWeakListener(wirelessSwitchesConsumer)
        extensionContext.serviceClient.registerPreferencesWeakListener(preferencesConsumer)
    }

    override fun onRideStart() {
        checkBatteriesAndNotify()
    }

    override fun onRideResume() {
        checkBatteriesAndNotify()
    }

    override fun onRideEnd() {
        handler.post {
            handler.removeCallbacks(showNextAlertRunnable)
            alertQueue.clear()
            alertShowing = false
        }
        alertMap.values.forEach { alertRecord ->
            alertRecord.alertedInRide = false
        }
    }

    private fun checkBatteriesAndNotify() {
        if (!alertsEnabled) {
            return
        }

        val lowBatteryRecords = alertMap.values
            .filter { it.wirelessSwitchInfo.batteryLevel == WirelessSwitchBatteryLevel.LOW }
            .sortedWith(compareBy({ it.deviceId.uid }, { it.wirelessSwitchInfo.slot }))

        if (rideState is RideState.Recording) {
            val pendingAlertRecords = lowBatteryRecords.filter { !it.alertedInRide }
            if (pendingAlertRecords.isNotEmpty()) {
                pendingAlertRecords.forEach { it.alertedInRide = true }
                handler.post {
                    alertQueue.addAll(pendingAlertRecords)
                    if (!alertShowing) {
                        showNextAlert()
                    }
                }
            }
        }

        if (lowBatteryRecords.any { !it.notified }) {
            lowBatteryRecords.forEach { it.notified = true }
            extensionContext.karooSystem.dispatch(
                SystemNotification(
                    "ki2-notification-switch-battery",
                    lowBatteryRecords.joinToString("\n") { getDescription(it) },
                    header = getTitle(lowBatteryRecords.size),
                    style = SystemNotification.Style.ERROR
                )
            )
        }
    }

    private fun showNextAlert() {
        alertShowing = false

        while (alertQueue.isNotEmpty()) {
            val alertRecord = alertQueue.removeFirst()

            if (alertRecord !in alertMap.values ||
                alertRecord.wirelessSwitchInfo.batteryLevel != WirelessSwitchBatteryLevel.LOW) {
                continue
            }

            if (rideState !is RideState.Recording) {
                alertRecord.alertedInRide = false
                continue
            }

            alertInRide(alertRecord)
            alertShowing = true
            handler.postDelayed(showNextAlertRunnable, ALERT_DURATION_MS + ALERT_GAP_MS)
            return
        }
    }

    private fun alertInRide(alertRecord: WirelessSwitchBatteryAlertRecord) {
        extensionContext.karooSystem.dispatch(
            InRideAlert(
                "ki2-ride-alert-switch-battery-${alertRecord.deviceId.uid}-${alertRecord.wirelessSwitchInfo.slot}",
                R.drawable.ic_hh_battery,
                getTitle(1),
                getDescription(alertRecord),
                ALERT_DURATION_MS,
                backgroundColor = R.color.hh_red_600,
                textColor = R.color.white
            )
        )
        extensionContext.audioManager.playKarooDeviceWarning()
    }

    private fun getTitle(count: Int): String {
        return extensionContext.context.resources.getQuantityString(R.plurals.text_low_switch_battery_header, count)
    }

    private fun getDescription(alertRecord: WirelessSwitchBatteryAlertRecord): String {
        return extensionContext.context.getString(
            R.string.text_param_switch_low_battery,
            extensionContext.getDeviceName(alertRecord.deviceId),
            WirelessSwitchName.getName(extensionContext.context, alertRecord.wirelessSwitchInfo)
        )
    }

}
