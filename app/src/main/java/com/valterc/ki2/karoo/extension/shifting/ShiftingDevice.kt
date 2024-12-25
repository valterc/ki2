package com.valterc.ki2.karoo.extension.shifting

import com.valterc.ki2.data.connection.ConnectionInfo
import com.valterc.ki2.data.device.BatteryInfo
import com.valterc.ki2.data.device.DeviceId
import com.valterc.ki2.data.device.DeviceName
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
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber
import java.time.Instant
import java.time.Instant.now
import java.util.concurrent.locks.Lock
import java.util.concurrent.locks.ReentrantLock
import java.util.function.BiConsumer
import java.util.function.Consumer
import kotlin.concurrent.withLock

class ShiftingDevice(
    private val extension: String,
    private val extensionContext: Ki2ExtensionContext,
    val deviceId: DeviceId
) {
    val source by lazy {
        Device(
            extension,
            deviceId.uid,
            listOf(
                DataType.Source.SHIFTING_FRONT_GEAR,
                DataType.Source.SHIFTING_REAR_GEAR,
                DataType.Source.SHIFTING_BATTERY
            ),
            extensionContext.serviceClient.getDevicePreferences(deviceId)
                ?.getName(extensionContext.context)
                ?: DeviceName.getDefaultName(extensionContext.context, deviceId)
        )
    }

    private val shiftingGearingHelper = ShiftingGearingHelper(extensionContext.context)
    private var devicePreferencesView: DevicePreferencesView? = null
    private var preferencesView: PreferencesView? = null
    private var batteryStatus: BatteryStatus? = null

    private val lock: ReentrantLock = ReentrantLock();
    private var timestampLastEmittedDataPoints: Instant? = null
    private var frontGearFieldMap: MutableMap<String, Double>? = null
    private var rearGearFieldMap: MutableMap<String, Double>? = null

    private val preferencesListener = Consumer<PreferencesView> { preferences: PreferencesView ->
        preferencesView = preferences
    }

    fun connect(emitter: Emitter<DeviceEvent>) {
        val devicePreferencesListener =
            BiConsumer<DeviceId, DevicePreferencesView> { deviceId: DeviceId, preferences: DevicePreferencesView ->
                if (this@ShiftingDevice.deviceId != deviceId) {
                    return@BiConsumer
                }

                devicePreferencesView = preferences

                if (!preferences.isEnabled(extensionContext.context)) {
                    emitter.onNext(OnConnectionStatus(ConnectionStatus.DISABLED))
                }
            }

        val connectionInfoListener =
            BiConsumer<DeviceId, ConnectionInfo> { deviceId: DeviceId, connectionInfo: ConnectionInfo ->
                if (this@ShiftingDevice.deviceId != deviceId) {
                    return@BiConsumer
                }

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
                if (this@ShiftingDevice.deviceId != deviceId) {
                    return@BiConsumer
                }

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
                if (this@ShiftingDevice.deviceId != deviceId) {
                    return@BiConsumer
                }

                shiftingGearingHelper.setShiftingInfo(shiftingInfo)

                val frontGearFieldMap = mutableMapOf(
                    DataType.Field.SHIFTING_FRONT_GEAR to shiftingGearingHelper.frontGear.toDouble(),
                    DataType.Field.SHIFTING_FRONT_GEAR_MAX to shiftingGearingHelper.frontGearMax.toDouble(),
                )

                if (shiftingGearingHelper.hasFrontGearSize()) {
                    frontGearFieldMap[DataType.Field.SHIFTING_FRONT_GEAR_TEETH] =
                        shiftingGearingHelper.frontGearTeethCount.toDouble()
                }

                val rearGearFieldMap = mutableMapOf(
                    DataType.Field.SHIFTING_REAR_GEAR to shiftingGearingHelper.rearGear.toDouble(),
                    DataType.Field.SHIFTING_REAR_GEAR_MAX to shiftingGearingHelper.rearGearMax.toDouble(),
                )

                if (shiftingGearingHelper.hasRearGearSize()) {
                    rearGearFieldMap[DataType.Field.SHIFTING_REAR_GEAR_TEETH] =
                        shiftingGearingHelper.rearGearTeethCount.toDouble()
                }

                lock.withLock {
                    emitDataPoints(emitter, frontGearFieldMap, rearGearFieldMap)
                }
            }

        Timber.i("[%s] Device connect", deviceId.uid)

        val job = CoroutineScope(Dispatchers.IO).launch {
            emitter.onNext(OnConnectionStatus(ConnectionStatus.SEARCHING))
            delay(5_000)
            extensionContext.serviceClient.registerPreferencesWeakListener(preferencesListener)
            extensionContext.serviceClient.registerDevicePreferencesWeakListener(
                devicePreferencesListener
            )
            extensionContext.serviceClient.registerConnectionInfoWeakListener(connectionInfoListener)
            extensionContext.serviceClient.registerBatteryInfoWeakListener(batteryInfoListener)
            extensionContext.serviceClient.registerShiftingInfoWeakListener(shiftingInfoListener)

            while (true) {
                lock.withLock {
                    timestampLastEmittedDataPoints?.let { timestampLastEmittedDataPoints ->
                        if (timestampLastEmittedDataPoints.isBefore(now().minusMillis(50_000))) {
                            frontGearFieldMap?.let { frontGearFieldMap ->
                                rearGearFieldMap?.let { rearGearFieldMap ->
                                    emitDataPoints(emitter, frontGearFieldMap, rearGearFieldMap)
                                }
                            }
                        }
                    }
                }

                delay(10_000)
            }
        }

        emitter.setCancellable {
            Timber.i("[%s] Device disconnect", deviceId.uid)
            job.cancel()
            extensionContext.serviceClient.unregisterPreferencesWeakListener(preferencesListener)
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

    private fun emitDataPoints(
        emitter: Emitter<DeviceEvent>,
        frontGearFieldMap: MutableMap<String, Double>,
        rearGearFieldMap: MutableMap<String, Double>
    ) {
        if (!lock.isHeldByCurrentThread){
            Timber.e("Emit Data Points lock not held!!")
        }

        timestampLastEmittedDataPoints = now()
        this.frontGearFieldMap = frontGearFieldMap
        this.rearGearFieldMap = rearGearFieldMap

        emitter.onNext(
            OnDataPoint(
                DataPoint(
                    DataType.Type.SHIFTING_FRONT_GEAR,
                    frontGearFieldMap,
                    source.uid
                )
            )
        )

        emitter.onNext(
            OnDataPoint(
                DataPoint(
                    DataType.Type.SHIFTING_REAR_GEAR,
                    rearGearFieldMap,
                    source.uid
                )
            )
        )

        batteryStatus?.let { batteryStatus ->
            emitter.onNext(OnBatteryStatus(batteryStatus))
            emitter.onNext(
                OnDataPoint(
                    DataPoint(
                        DataType.Type.SHIFTING_BATTERY,
                        mapOf(
                            DataType.Field.SHIFTING_BATTERY_STATUS to batteryStatus.ordinal.toDouble()
                        ),
                        source.uid
                    )
                )
            )
        }
    }
}