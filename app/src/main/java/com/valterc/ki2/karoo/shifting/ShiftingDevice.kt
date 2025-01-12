package com.valterc.ki2.karoo.shifting

import com.valterc.ki2.data.connection.ConnectionInfo
import com.valterc.ki2.data.connection.ConnectionStatus
import com.valterc.ki2.data.device.BatteryInfo
import com.valterc.ki2.data.device.DeviceId
import com.valterc.ki2.data.device.DeviceName
import com.valterc.ki2.data.preferences.PreferencesView
import com.valterc.ki2.data.preferences.device.DevicePreferencesView
import com.valterc.ki2.data.shifting.ShiftingInfo
import com.valterc.ki2.karoo.Ki2ExtensionContext
import com.valterc.ki2.karoo.datatypes.Ki2DataType
import io.hammerhead.karooext.internal.Emitter
import io.hammerhead.karooext.models.BatteryStatus
import io.hammerhead.karooext.models.ConnectionStatus as KarooConnectionStatus
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
import java.util.concurrent.locks.ReentrantLock
import java.util.function.BiConsumer
import java.util.function.Consumer
import kotlin.concurrent.withLock

class ShiftingDevice(
    private val extensionContext: Ki2ExtensionContext,
    val deviceId: DeviceId
) {
    val source by lazy {
        Device(
            extensionContext.extension,
            deviceId.uid,
            listOf(
                DataType.Source.SHIFTING_FRONT_GEAR,
                DataType.Source.SHIFTING_REAR_GEAR,
                DataType.Source.SHIFTING_BATTERY,
                DataType.dataTypeId(extensionContext.extension, Ki2DataType.Type.DFLY),
                DataType.dataTypeId(extensionContext.extension, Ki2DataType.Type.DI2),
                DataType.dataTypeId(extensionContext.extension, Ki2DataType.Type.STEPS)
            ),
            extensionContext.serviceClient.getDevicePreferences(deviceId)
                ?.getName(extensionContext.context)
                ?: DeviceName.getDefaultName(extensionContext.context, deviceId)
        )
    }

    private val shiftingGearingHelper =
        ShiftingGearingHelper(extensionContext.context)
    private var preferencesView: PreferencesView? = null
    private var devicePreferencesView: DevicePreferencesView? = null
    private var connectionInfo: ConnectionInfo? = null
    private var batteryStatus: BatteryStatus? = null
    private var batteryInfo: BatteryInfo? = null

    private val lock: ReentrantLock = ReentrantLock()
    private var timestampLastEmittedDataPoints: Instant? = null

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
                    emitter.onNext(OnConnectionStatus(KarooConnectionStatus.DISABLED))
                }

                lock.withLock {
                    shiftingGearingHelper.setDevicePreferences(preferences)
                    emitDataPoints(emitter)
                }
            }

        val connectionInfoListener =
            BiConsumer<DeviceId, ConnectionInfo> { deviceId: DeviceId, connectionInfo: ConnectionInfo ->
                if (this@ShiftingDevice.deviceId != deviceId) {
                    return@BiConsumer
                }

                this.connectionInfo = connectionInfo

                devicePreferencesView?.let { devicePreferencesView ->
                    if (!devicePreferencesView.isEnabled(extensionContext.context)) {
                        emitter.onNext(OnConnectionStatus(KarooConnectionStatus.DISABLED))
                        return@BiConsumer
                    }
                }

                when (connectionInfo.connectionStatus) {
                    ConnectionStatus.INVALID -> emitter.onNext(
                        OnConnectionStatus(KarooConnectionStatus.DISCONNECTED)
                    )

                    ConnectionStatus.NEW -> emitter.onNext(
                        OnConnectionStatus(KarooConnectionStatus.SEARCHING)
                    )

                    ConnectionStatus.CONNECTING -> emitter.onNext(
                        OnConnectionStatus(KarooConnectionStatus.SEARCHING)
                    )

                    ConnectionStatus.ESTABLISHED -> emitter.onNext(
                        OnConnectionStatus(KarooConnectionStatus.CONNECTED)
                    )

                    ConnectionStatus.CLOSED -> emitter.onNext(
                        OnConnectionStatus(KarooConnectionStatus.DISCONNECTED)
                    )

                    else -> emitter.onNext(OnConnectionStatus(KarooConnectionStatus.DISCONNECTED))
                }

                if (connectionInfo.connectionStatus == ConnectionStatus.ESTABLISHED) {
                    emitDataPoints(emitter)
                }
            }

        val batteryInfoListener =
            BiConsumer<DeviceId, BatteryInfo> { deviceId: DeviceId, batteryInfo: BatteryInfo ->
                if (this@ShiftingDevice.deviceId != deviceId) {
                    return@BiConsumer
                }

                this.batteryInfo = batteryInfo

                val lowLevel = preferencesView?.getBatteryLevelLow(extensionContext.context)
                val criticalLevel =
                    preferencesView?.getBatteryLevelCritical(extensionContext.context)

                batteryStatus = when {
                    batteryInfo.value >= 80 -> BatteryStatus.NEW
                    batteryInfo.value >= 60 -> BatteryStatus.GOOD
                    criticalLevel != null && batteryInfo.value <= criticalLevel -> BatteryStatus.CRITICAL
                    lowLevel != null && batteryInfo.value <= lowLevel -> BatteryStatus.LOW
                    else -> BatteryStatus.OK
                }

                emitDataPoints(emitter)
            }

        val shiftingInfoListener =
            BiConsumer<DeviceId, ShiftingInfo> { deviceId: DeviceId, shiftingInfo: ShiftingInfo ->
                if (this@ShiftingDevice.deviceId != deviceId) {
                    return@BiConsumer
                }

                lock.withLock {
                    shiftingGearingHelper.setShiftingInfo(shiftingInfo)
                    emitDataPoints(emitter)
                }
            }

        Timber.i("[%s] Device connect", deviceId.uid)

        val job = CoroutineScope(Dispatchers.IO).launch {
            emitter.onNext(OnConnectionStatus(KarooConnectionStatus.SEARCHING))
            delay(5_000)
            extensionContext.serviceClient.registerPreferencesWeakListener(preferencesListener)
            extensionContext.serviceClient.registerUnfilteredDevicePreferencesWeakListener(
                devicePreferencesListener
            )
            extensionContext.serviceClient.registerUnfilteredConnectionInfoWeakListener(
                connectionInfoListener
            )
            extensionContext.serviceClient.registerUnfilteredBatteryInfoWeakListener(
                batteryInfoListener
            )
            extensionContext.serviceClient.registerUnfilteredShiftingInfoWeakListener(
                shiftingInfoListener
            )

            while (true) {
                lock.withLock {
                    timestampLastEmittedDataPoints?.let { timestampLastEmittedDataPoints ->
                        if (timestampLastEmittedDataPoints.isBefore(now().minusMillis(50_000))) {
                            emitDataPoints(emitter)
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
            extensionContext.serviceClient.unregisterUnfilteredDevicePreferencesWeakListener(
                devicePreferencesListener
            )
            extensionContext.serviceClient.unregisterUnfilteredConnectionInfoWeakListener(
                connectionInfoListener
            )
            extensionContext.serviceClient.unregisterUnfilteredBatteryInfoWeakListener(
                batteryInfoListener
            )
            extensionContext.serviceClient.unregisterUnfilteredShiftingInfoWeakListener(
                shiftingInfoListener
            )
        }
    }

    private fun emitDataPoints(emitter: Emitter<DeviceEvent>) {
        if (devicePreferencesView?.isEnabled(extensionContext.context) == false || connectionInfo?.isConnected == false) {
            return
        }

        timestampLastEmittedDataPoints = now()

        emitKarooDataPoints(emitter)
        emitDi2DataPoints(emitter)
        emitSTEPSDataPoints(emitter)
    }

    private fun emitKarooDataPoints(emitter: Emitter<DeviceEvent>) {
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

    private fun emitDi2DataPoints(emitter: Emitter<DeviceEvent>) {
        if (shiftingGearingHelper.hasInvalidGearingInfo()) {
            return
        }

        val fieldMap = mutableMapOf(
            Ki2DataType.Field.DI2_FRONT_GEAR_INDEX to shiftingGearingHelper.frontGear.toDouble(),
            Ki2DataType.Field.DI2_FRONT_GEAR_MAX to shiftingGearingHelper.frontGearMax.toDouble(),

            Ki2DataType.Field.DI2_REAR_GEAR_INDEX to shiftingGearingHelper.rearGear.toDouble(),
            Ki2DataType.Field.DI2_REAR_GEAR_MAX to shiftingGearingHelper.rearGearMax.toDouble(),
        )

        if (shiftingGearingHelper.hasFrontGearSize()) {
            fieldMap[Ki2DataType.Field.DI2_FRONT_GEAR_TEETH] =
                shiftingGearingHelper.frontGearTeethCount.toDouble()
        }

        if (shiftingGearingHelper.hasRearGearSize()) {
            fieldMap[Ki2DataType.Field.DI2_REAR_GEAR_TEETH] =
                shiftingGearingHelper.rearGearTeethCount.toDouble()
        }

        batteryInfo?.let { batteryInfo ->
            fieldMap[Ki2DataType.Field.DI2_BATTERY] = batteryInfo.value.toDouble()
        }

        fieldMap[Ki2DataType.Field.DI2_SHIFTING_MODE] =
            shiftingGearingHelper.shiftingMode.value.toDouble()

        emitter.onNext(
            OnDataPoint(
                DataPoint(
                    DataType.dataTypeId(extensionContext.extension, Ki2DataType.Type.DI2),
                    fieldMap,
                    source.uid
                )
            )
        )
    }

    private fun emitSTEPSDataPoints(emitter: Emitter<DeviceEvent>) {
        if (batteryInfo == null) {
            return
        }

        val fieldMap = mutableMapOf<String, Double>()

        batteryInfo?.let { batteryInfo ->
            fieldMap[Ki2DataType.Field.STEPS_BATTERY] = batteryInfo.value.toDouble()
        }

        emitter.onNext(
            OnDataPoint(
                DataPoint(
                    DataType.dataTypeId(extensionContext.extension, Ki2DataType.Type.STEPS),
                    fieldMap,
                    source.uid
                )
            )
        )
    }
}