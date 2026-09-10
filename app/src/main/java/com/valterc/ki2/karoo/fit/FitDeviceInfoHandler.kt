package com.valterc.ki2.karoo.fit

import com.valterc.ki2.data.connection.ConnectionInfo
import com.valterc.ki2.data.connection.ConnectionStatus
import com.valterc.ki2.data.device.DeviceId
import com.valterc.ki2.karoo.Ki2ExtensionContext
import io.hammerhead.karooext.internal.Emitter
import io.hammerhead.karooext.models.DeveloperField
import io.hammerhead.karooext.models.FieldValue
import io.hammerhead.karooext.models.FitEffect
import io.hammerhead.karooext.models.RideState
import io.hammerhead.karooext.models.WriteToSessionMesg
import timber.log.Timber
import java.util.function.BiConsumer

/**
 * Writes the ANT identifiers of the Ki2 device in use into the FIT file recorded by the Karoo so
 * that post-ride tools are able to identify the drivetrain used on a ride.
 *
 * The identifiers mirror the fields the FIT profile defines on the native device info message,
 * which extensions are not allowed to write. Values are written to the session message, matching
 * the message that other Karoo extensions use for developer fields.
 */
class FitDeviceInfoHandler(private val extensionContext: Ki2ExtensionContext) {

    companion object {
        private const val FIT_BASE_TYPE_UINT8: Short = 2
        private const val FIT_BASE_TYPE_UINT16: Short = 132
    }

    private val deviceNumberField =
        DeveloperField(0, FIT_BASE_TYPE_UINT16, "ki2_ant_device_number", "")
    private val deviceTypeField =
        DeveloperField(1, FIT_BASE_TYPE_UINT8, "ki2_ant_device_type", "")
    private val transmissionTypeField =
        DeveloperField(2, FIT_BASE_TYPE_UINT8, "ki2_ant_transmission_type", "")

    private var deviceInUse: DeviceId? = null
    private var rideState: RideState = RideState.Idle
    private var emitter: Emitter<FitEffect>? = null

    private val connectionInfoConsumer =
        BiConsumer<DeviceId, ConnectionInfo> { deviceId: DeviceId, connectionInfo: ConnectionInfo ->
            when {
                connectionInfo.connectionStatus == ConnectionStatus.ESTABLISHED -> {
                    if (deviceInUse != deviceId) {
                        deviceInUse = deviceId
                        writeDeviceInfo()
                    }
                }

                deviceInUse == deviceId -> deviceInUse = null
            }
        }

    fun start(emitter: Emitter<FitEffect>) {
        Timber.i("FIT device info writing started")
        this.emitter = emitter

        extensionContext.serviceClient.registerConnectionInfoWeakListener(connectionInfoConsumer)

        val consumerId = extensionContext.karooSystem.addConsumer { newRideState: RideState ->
            val oldRideState = rideState
            rideState = newRideState

            if (newRideState is RideState.Recording && oldRideState is RideState.Idle) {
                writeDeviceInfo()
            }
        }

        emitter.setCancellable {
            Timber.i("FIT device info writing stopped")
            extensionContext.karooSystem.removeConsumer(consumerId)
            extensionContext.serviceClient.unregisterConnectionInfoWeakListener(
                connectionInfoConsumer
            )
            this.emitter = null
        }
    }

    private fun writeDeviceInfo() {
        if (rideState is RideState.Idle) {
            return
        }

        val emitter = this.emitter ?: return
        val deviceId = deviceInUse ?: return

        Timber.d("Writing FIT device info for device %s", deviceId.uid)

        emitter.onNext(
            WriteToSessionMesg(
                listOf(
                    FieldValue(deviceNumberField, deviceId.deviceNumber.toDouble()),
                    FieldValue(deviceTypeField, deviceId.deviceTypeValue.toDouble()),
                    FieldValue(transmissionTypeField, deviceId.transmissionType.toDouble())
                )
            )
        )
    }

}
