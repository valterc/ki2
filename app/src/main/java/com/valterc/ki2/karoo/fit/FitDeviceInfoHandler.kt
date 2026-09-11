package com.valterc.ki2.karoo.fit

import com.valterc.ki2.BuildConfig
import com.valterc.ki2.data.connection.ConnectionInfo
import com.valterc.ki2.data.connection.ConnectionStatus
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
 * Writes the Ki2 version and the ANT identifiers of the Ki2 device in use into the FIT file
 * recorded by the Karoo so that post-ride tools are able to identify the drivetrain used on a ride.
 *
 * The identifiers mirror the fields the FIT profile defines on the native device info message,
 * which extensions are not allowed to write. They are instead written as developer fields on
 * fitness equipment event messages, a start event when the device starts being used and a stop
 * event when it stops being used, so that the period the device was in use is timestamped.
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
        DeveloperField(1, FIT_BASE_TYPE_UINT16, "ki2_ant_device_number", "")
    private val deviceTypeField =
        DeveloperField(2, FIT_BASE_TYPE_UINT8, "ki2_ant_device_type", "")
    private val transmissionTypeField =
        DeveloperField(3, FIT_BASE_TYPE_UINT8, "ki2_ant_transmission_type", "")

    private var connectedDevice: DeviceId? = null
    private var writtenDevice: DeviceId? = null
    private var enabled: Boolean = false
    private var emitter: Emitter<FitEffect>? = null

    private val connectionInfoConsumer =
        BiConsumer<DeviceId, ConnectionInfo> { deviceId: DeviceId, connectionInfo: ConnectionInfo ->
            when {
                connectionInfo.connectionStatus == ConnectionStatus.ESTABLISHED -> {
                    if (connectedDevice != deviceId) {
                        connectedDevice = deviceId
                        writeDeviceInfo()
                    }
                }

                connectedDevice == deviceId -> {
                    connectedDevice = null
                    writeDeviceInfo()
                }
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

        emitter.setCancellable {
            Timber.i("FIT device info writing stopped")
            extensionContext.serviceClient.unregisterConnectionInfoWeakListener(
                connectionInfoConsumer
            )
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

        val deviceId = if (enabled) connectedDevice else null
        val oldDeviceId = writtenDevice

        if (oldDeviceId == deviceId) {
            return
        }

        if (oldDeviceId != null) {
            Timber.d("Writing FIT event Stop for device info for device %s", oldDeviceId.uid)

            emitter.onNext(
                WriteEventMesg(
                    FIT_EVENT_FITNESS_EQUIPMENT,
                    FIT_EVENT_TYPE_STOP,
                    getFieldValues(oldDeviceId)
                )
            )
        }

        if (deviceId != null) {
            Timber.d("Writing FIT event Start for device info for device %s", deviceId.uid)

            emitter.onNext(
                WriteEventMesg(
                    FIT_EVENT_FITNESS_EQUIPMENT,
                    FIT_EVENT_TYPE_START,
                    getFieldValues(deviceId)
                )
            )
        }

        writtenDevice = deviceId
    }

    private fun getFieldValues(deviceId: DeviceId) = listOf(
        FieldValue(versionField, BuildConfig.VERSION_CODE.toDouble()),
        FieldValue(deviceNumberField, deviceId.deviceNumber.toDouble()),
        FieldValue(deviceTypeField, deviceId.deviceTypeValue.toDouble()),
        FieldValue(transmissionTypeField, deviceId.transmissionType.toDouble())
    )

}
