package com.valterc.ki2.karoo.fit

import com.valterc.ki2.BuildConfig
import com.valterc.ki2.data.connection.ConnectionInfo
import com.valterc.ki2.data.connection.ConnectionStatus
import com.valterc.ki2.data.device.BatteryInfo
import com.valterc.ki2.data.device.DeviceId
import com.valterc.ki2.data.preferences.PreferencesView
import com.valterc.ki2.karoo.Ki2ExtensionContext
import com.valterc.ki2.karoo.RideHandler
import io.hammerhead.karooext.internal.Emitter
import io.hammerhead.karooext.models.DeveloperField
import io.hammerhead.karooext.models.FieldValue
import io.hammerhead.karooext.models.FitEffect
import io.hammerhead.karooext.models.RideState
import io.hammerhead.karooext.models.WriteEventMesg
import timber.log.Timber
import java.util.function.BiConsumer
import java.util.function.Consumer

/**
 * Writes the Ki2 version, the ANT identifiers and the battery level of the Ki2 device in use into
 * the FIT file recorded by the Karoo so that post-ride tools are able to identify the drivetrain
 * used on a ride and how much battery it had.
 *
 * The identifiers mirror the fields the FIT profile defines on the native device info message,
 * which extensions are not allowed to write. They are instead written as developer fields on
 * fitness equipment event messages, a start event when the device starts being used and a stop
 * event when it stops being used, so that the period the device was in use is timestamped. The
 * battery level of the device is written on both events, giving the battery level at the start and
 * at the end of the period the device was in use.
 *
 * The battery level is only known once the device reports it, shortly after connecting, so the
 * start event is only written once a battery level is available for the device in use.
 *
 * Shift events are not written by Ki2, they are recorded by the Karoo when the Ki2 sensors are
 * added to the Karoo sensors.
 *
 * This is only done while a ride is in progress and when the FIT recording setting is enabled.
 * Disabling the setting mid-ride writes the stop event for the device in use.
 */
class FitDeviceInfoHandler(extensionContext: Ki2ExtensionContext) : RideHandler(extensionContext) {

    companion object {
        private const val FIT_BASE_TYPE_UINT8: Short = 2
        private const val FIT_BASE_TYPE_UINT16: Short = 132
        private const val FIT_BASE_TYPE_UINT32: Short = 134

        private const val FIT_EVENT_FITNESS_EQUIPMENT: Short = 27

        private const val FIT_EVENT_TYPE_START: Short = 0
        private const val FIT_EVENT_TYPE_STOP: Short = 1
    }

    private val versionField =
        DeveloperField(0, FIT_BASE_TYPE_UINT32, "ki2_version", "")
    private val deviceNumberField =
        DeveloperField(1, FIT_BASE_TYPE_UINT16, "ki2_di2_ant_device_number", "")
    private val deviceTypeField =
        DeveloperField(2, FIT_BASE_TYPE_UINT8, "ki2_di2_ant_device_type", "")
    private val transmissionTypeField =
        DeveloperField(3, FIT_BASE_TYPE_UINT8, "ki2_di2_ant_transmission_type", "")
    private val batteryLevelField =
        DeveloperField(4, FIT_BASE_TYPE_UINT8, "ki2_di2_battery_level", "percent")

    /**
     * Data of a device that is written into the FIT file.
     */
    private data class DeviceData(
        val deviceId: DeviceId,
        val batteryLevel: Int? = null,
    )

    private var connectedDevice: DeviceData? = null
    private var writtenDevice: DeviceData? = null
    private var enabled: Boolean = false
    private var emitter: Emitter<FitEffect>? = null

    private val connectionInfoConsumer =
        BiConsumer<DeviceId, ConnectionInfo> { deviceId: DeviceId, connectionInfo: ConnectionInfo ->
            when {
                connectionInfo.connectionStatus == ConnectionStatus.ESTABLISHED -> {
                    if (connectedDevice?.deviceId != deviceId) {
                        connectedDevice = DeviceData(deviceId)
                        writeDeviceInfo()
                    }
                }

                connectedDevice?.deviceId == deviceId -> {
                    connectedDevice = null
                    writeDeviceInfo()
                }
            }
        }

    private val batteryInfoConsumer =
        BiConsumer<DeviceId, BatteryInfo> { deviceId: DeviceId, batteryInfo: BatteryInfo ->
            val connectedDevice = this.connectedDevice ?: return@BiConsumer

            if (connectedDevice.deviceId != deviceId) {
                return@BiConsumer
            }

            this.connectedDevice = connectedDevice.copy(batteryLevel = batteryInfo.value)

            val writtenDevice = this.writtenDevice
            if (writtenDevice?.deviceId == deviceId) {
                this.writtenDevice = writtenDevice.copy(batteryLevel = batteryInfo.value)
            } else {
                writeDeviceInfo()
            }
        }

    private val preferencesConsumer = Consumer<PreferencesView> { preferences ->
        val newEnabled = preferences.isFitRecordingDeviceInfoEnabled(extensionContext.context)

        if (enabled != newEnabled) {
            enabled = newEnabled
            writeDeviceInfo()
        }
    }

    init {
        extensionContext.serviceClient.registerPreferencesWeakListener(preferencesConsumer)
    }

    fun start(emitter: Emitter<FitEffect>) {
        Timber.i("FIT device info writing started")
        this.emitter = emitter

        extensionContext.serviceClient.registerConnectionInfoWeakListener(connectionInfoConsumer)
        extensionContext.serviceClient.registerBatteryInfoWeakListener(batteryInfoConsumer)

        emitter.setCancellable {
            Timber.i("FIT device info writing stopped")
            extensionContext.serviceClient.unregisterConnectionInfoWeakListener(
                connectionInfoConsumer
            )
            extensionContext.serviceClient.unregisterBatteryInfoWeakListener(batteryInfoConsumer)
            this.emitter = null
            this.writtenDevice = null
        }

        writeDeviceInfo()
    }

    override fun onRideStart() {
        writeDeviceInfo()
    }

    override fun onRideEnd() {
        writtenDevice = null
    }

    private fun writeDeviceInfo() {
        val emitter = this.emitter ?: return

        if (rideState is RideState.Idle) {
            return
        }

        val device = if (enabled) connectedDevice else null
        val oldDevice = writtenDevice

        if (oldDevice?.deviceId == device?.deviceId) {
            return
        }

        if (oldDevice != null) {
            Timber.d("Writing FIT event Stop for device info for device %s", oldDevice.deviceId.uid)

            emitter.onNext(
                WriteEventMesg(
                    FIT_EVENT_FITNESS_EQUIPMENT,
                    FIT_EVENT_TYPE_STOP,
                    getFieldValues(oldDevice)
                )
            )

            writtenDevice = null
        }

        if (device != null) {
            if (device.batteryLevel == null) {
                return
            }

            Timber.d("Writing FIT event Start for device info for device %s", device.deviceId.uid)

            emitter.onNext(
                WriteEventMesg(
                    FIT_EVENT_FITNESS_EQUIPMENT,
                    FIT_EVENT_TYPE_START,
                    getFieldValues(device)
                )
            )

            writtenDevice = device
        }
    }

    private fun getFieldValues(device: DeviceData) = listOfNotNull(
        FieldValue(versionField, BuildConfig.VERSION_CODE.toDouble()),
        FieldValue(deviceNumberField, device.deviceId.deviceNumber.toDouble()),
        FieldValue(deviceTypeField, device.deviceId.deviceTypeValue.toDouble()),
        FieldValue(transmissionTypeField, device.deviceId.transmissionType.toDouble()),
        device.batteryLevel?.let { FieldValue(batteryLevelField, it.toDouble()) }
    )

}
