package com.valterc.ki2.karoo.extension.shifting

import com.valterc.ki2.data.connection.ConnectionInfo
import com.valterc.ki2.data.device.BatteryInfo
import com.valterc.ki2.data.device.DeviceId
import com.valterc.ki2.data.preferences.PreferencesView
import com.valterc.ki2.data.preferences.device.DevicePreferencesView
import com.valterc.ki2.data.shifting.ShiftingInfo
import com.valterc.ki2.karoo.extension.Ki2ExtensionContext
import io.hammerhead.karooext.internal.Emitter
import io.hammerhead.karooext.models.BatteryStatus
import io.hammerhead.karooext.models.ConnectionStatus
import io.hammerhead.karooext.models.DataPoint
import io.hammerhead.karooext.models.DataType
import io.hammerhead.karooext.models.Device
import io.hammerhead.karooext.models.DeviceEvent
import io.hammerhead.karooext.models.OnBatteryStatus
import io.hammerhead.karooext.models.OnConnectionStatus
import io.hammerhead.karooext.models.OnDataPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber
import java.lang.Thread.sleep
import java.util.function.BiConsumer
import java.util.function.Consumer

class DefaultShiftingDevice(
    private val extension: String,
    private val extensionContext: Ki2ExtensionContext
) {
    val source by lazy {
        Device(
            extension,
            "default",
            listOf(
                DataType.Source.SHIFTING_FRONT_GEAR,
                DataType.Source.SHIFTING_REAR_GEAR,
                DataType.Source.SHIFTING_BATTERY
            ),
            "Ki2"
        )
    }

    fun connect(emitter: Emitter<DeviceEvent>) {
        var devicePreferencesView: DevicePreferencesView? = null
        var preferencesView: PreferencesView? = null
        var batteryStatus: BatteryStatus? = null
        val shiftingGearingHelper = ShiftingGearingHelper(extensionContext.context)

        val preferencesListener = Consumer<PreferencesView> { preferences: PreferencesView ->
            preferencesView = preferences
        }

        val devicePreferencesListener =
            BiConsumer<DeviceId, DevicePreferencesView> { _: DeviceId, preferences: DevicePreferencesView ->
                devicePreferencesView = preferences

                if (!preferences.isEnabled(extensionContext.context)) {
                    emitter.onNext(OnConnectionStatus(ConnectionStatus.DISABLED))
                }
            }

        val connectionInfoListener =
            BiConsumer<DeviceId, ConnectionInfo> { deviceId: DeviceId, connectionInfo: ConnectionInfo ->
                Timber.i(
                    "[%s] Emitting connection %s",
                    deviceId.uid,
                    connectionInfo.connectionStatus
                )

                devicePreferencesView?.let {
                    if (!it.isEnabled(extensionContext.context)) {
                        emitter.onNext(OnConnectionStatus(ConnectionStatus.DISABLED))
                        return@BiConsumer
                    }
                }

                when (connectionInfo.connectionStatus) {
                    com.valterc.ki2.data.connection.ConnectionStatus.INVALID -> emitter.onNext(
                        OnConnectionStatus(ConnectionStatus.DISCONNECTED)
                    )

                    com.valterc.ki2.data.connection.ConnectionStatus.NEW -> emitter.onNext(
                        OnConnectionStatus(ConnectionStatus.SEARCHING)
                    )

                    com.valterc.ki2.data.connection.ConnectionStatus.CONNECTING -> emitter.onNext(
                        OnConnectionStatus(ConnectionStatus.SEARCHING)
                    )

                    com.valterc.ki2.data.connection.ConnectionStatus.ESTABLISHED -> emitter.onNext(
                        OnConnectionStatus(ConnectionStatus.CONNECTED)
                    )

                    com.valterc.ki2.data.connection.ConnectionStatus.CLOSED -> emitter.onNext(
                        OnConnectionStatus(ConnectionStatus.DISCONNECTED)
                    )

                    else -> emitter.onNext(OnConnectionStatus(ConnectionStatus.DISCONNECTED))
                }
            }

        val batteryInfoListener =
            BiConsumer<DeviceId, BatteryInfo> { deviceId: DeviceId, batteryInfo: BatteryInfo ->
                Timber.i("[%s] Emitting battery %s", deviceId.uid, batteryInfo.value)


                val lowLevel = preferencesView?.getBatteryLevelLow(extensionContext.context)
                val criticalLevel =
                    preferencesView?.getBatteryLevelCritical(extensionContext.context)

                batteryStatus = when {
                    batteryInfo.value >= 80 -> BatteryStatus.NEW
                    batteryInfo.value >= 50 -> BatteryStatus.GOOD
                    criticalLevel != null && batteryInfo.value <= criticalLevel -> BatteryStatus.CRITICAL
                    lowLevel != null && batteryInfo.value <= lowLevel -> BatteryStatus.LOW
                    else -> BatteryStatus.OK
                }

                batteryStatus?.let {
                    emitter.onNext(OnBatteryStatus(it))
                    emitter.onNext(
                        OnDataPoint(
                            DataPoint(
                                DataType.Type.SHIFTING_BATTERY,
                                mapOf(
                                    DataType.Field.SHIFTING_BATTERY_STATUS to it.ordinal.toDouble()
                                ),
                                source.uid
                            )
                        )
                    )
                }
            }

        val shiftingInfoListener =
            BiConsumer<DeviceId, ShiftingInfo> { deviceId: DeviceId, shiftingInfo: ShiftingInfo ->
                shiftingGearingHelper.setShiftingInfo(shiftingInfo)

                val frontGearFieldMap = mutableMapOf(
                    DataType.Field.SHIFTING_FRONT_GEAR to shiftingGearingHelper.frontGear.toDouble(),
                    DataType.Field.SHIFTING_FRONT_GEAR_MAX to shiftingGearingHelper.frontGearMax.toDouble(),
                )

                if (shiftingGearingHelper.hasFrontGearSize()) {
                    frontGearFieldMap[DataType.Field.SHIFTING_FRONT_GEAR_TEETH] =
                        shiftingGearingHelper.frontGearTeethCount.toDouble()
                }

                Timber.i("[%s] Emitting datapoint %s", deviceId.uid, frontGearFieldMap)
                emitter.onNext(
                    OnDataPoint(
                        DataPoint(
                            DataType.Type.SHIFTING_FRONT_GEAR,
                            frontGearFieldMap,
                            source.uid
                        )
                    )
                )

                val rearGearFieldMap = mutableMapOf(
                    DataType.Field.SHIFTING_REAR_GEAR to shiftingGearingHelper.rearGear.toDouble(),
                    DataType.Field.SHIFTING_REAR_GEAR_MAX to shiftingGearingHelper.rearGearMax.toDouble(),
                )

                if (shiftingGearingHelper.hasRearGearSize()) {
                    rearGearFieldMap[DataType.Field.SHIFTING_REAR_GEAR_TEETH] =
                        shiftingGearingHelper.rearGearTeethCount.toDouble()
                }

                Timber.i("[%s] Emitting datapoint %s", deviceId.uid, rearGearFieldMap)
                emitter.onNext(
                    OnDataPoint(
                        DataPoint(
                            DataType.Type.SHIFTING_REAR_GEAR,
                            rearGearFieldMap,
                            source.uid
                        )
                    )
                )

                batteryStatus?.let {
                    emitter.onNext(OnBatteryStatus(it))
                    emitter.onNext(
                        OnDataPoint(
                            DataPoint(
                                DataType.Type.SHIFTING_BATTERY,
                                mapOf(
                                    DataType.Field.SHIFTING_BATTERY_STATUS to it.ordinal.toDouble()
                                ),
                                source.uid
                            )
                        )
                    )
                }
            }

        Timber.i("Default device connect")

        val job = CoroutineScope(Dispatchers.IO).launch {
            emitter.onNext(OnConnectionStatus(ConnectionStatus.SEARCHING))
            sleep(5000)
            extensionContext.serviceClient.registerPreferencesWeakListener(preferencesListener)
            extensionContext.serviceClient.registerDevicePreferencesWeakListener(
                devicePreferencesListener
            )
            extensionContext.serviceClient.registerConnectionInfoWeakListener(connectionInfoListener)
            extensionContext.serviceClient.registerBatteryInfoWeakListener(batteryInfoListener)
            extensionContext.serviceClient.registerShiftingInfoWeakListener(shiftingInfoListener)
        }

        emitter.setCancellable {
            Timber.i("Default device disconnect")
            job.cancel()
            extensionContext.serviceClient.unregisterDevicePreferencesWeakListener(
                devicePreferencesListener
            )
            extensionContext.serviceClient.unregisterConnectionInfoWeakListener(
                connectionInfoListener
            )
            extensionContext.serviceClient.unregisterBatteryInfoWeakListener(batteryInfoListener)
            extensionContext.serviceClient.unregisterShiftingInfoWeakListener(shiftingInfoListener)
        }
    }
}