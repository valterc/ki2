package com.valterc.ki2.karoo.extension

import android.provider.ContactsContract.Data
import com.valterc.ki2.data.device.DeviceId
import io.hammerhead.karooext.internal.Emitter
import io.hammerhead.karooext.models.BatteryStatus
import io.hammerhead.karooext.models.ConnectionStatus
import io.hammerhead.karooext.models.DataPoint
import io.hammerhead.karooext.models.DataType
import io.hammerhead.karooext.models.Device
import io.hammerhead.karooext.models.DeviceEvent
import io.hammerhead.karooext.models.ManufacturerInfo
import io.hammerhead.karooext.models.OnBatteryStatus
import io.hammerhead.karooext.models.OnConnectionStatus
import io.hammerhead.karooext.models.OnDataPoint
import io.hammerhead.karooext.models.OnManufacturerInfo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber

class ShiftingDevice(
    private val extension: String,
    val deviceId: DeviceId
) {
    companion object {
        fun fromUid(extension: String, uid: String): ShiftingDevice {
            return ShiftingDevice(extension, DeviceId(uid))
        }
    }

    val source by lazy {
        Device(
            extension,
            deviceId.uid,
            listOf(
                DataType.Source.SHIFTING_FRONT_GEAR,
                DataType.Source.SHIFTING_REAR_GEAR
            ),
            deviceId.uid
        )
    }

    fun connect(emitter: Emitter<DeviceEvent>) {
        val job = CoroutineScope(Dispatchers.IO).launch {
            // 2s searching
            emitter.onNext(OnConnectionStatus(ConnectionStatus.SEARCHING))
            delay(2000)
            // Update device is now connected
            emitter.onNext(OnConnectionStatus(ConnectionStatus.CONNECTED))
            delay(1000)
            // Update battery status
            emitter.onNext(OnBatteryStatus(BatteryStatus.NEW))
            delay(1000)
            // Send manufacturer info
            emitter.onNext(OnManufacturerInfo(ManufacturerInfo("Test", "Test", "Test")))
            delay(1000)
            // Start streaming data
            repeat(Int.MAX_VALUE) {
                Timber.i("Sending data point for device %s", deviceId.uid)

                emitter.onNext(
                    OnDataPoint(
                        DataPoint(
                            DataType.Type.SHIFTING_FRONT_GEAR,
                            values = mapOf(
                                DataType.Field.SHIFTING_FRONT_GEAR to 1.0,
                                DataType.Field.SHIFTING_FRONT_GEAR_MAX to 2.0),
                            sourceId = source.uid,
                        ),
                    )
                )
                emitter.onNext(
                    OnDataPoint(
                        DataPoint(
                            DataType.Type.SHIFTING_REAR_GEAR,
                            values = mapOf(
                                DataType.Field.SHIFTING_REAR_GEAR to 2.0,
                                DataType.Field.SHIFTING_FRONT_GEAR_TEETH
                                DataType.Field.SHIFTING_REAR_GEAR_MAX to 2.0),
                            sourceId = source.uid,
                        ),
                    )
                )
                emitter.onNext(
                    OnDataPoint(
                        DataPoint(
                            DataType.Type.SHIFTING_BATTERY,
                            values = mapOf(
                                DataType.Field.SHIFTING_BATTERY_STATUS to 10.0),
                            sourceId = source.uid,
                        ),
                    )
                )
                delay(1000)
            }
            awaitCancellation()
        }

        emitter.setCancellable {
            Timber.i("On device disconnect %s", deviceId.uid)
            job.cancel()
        }
    }
}