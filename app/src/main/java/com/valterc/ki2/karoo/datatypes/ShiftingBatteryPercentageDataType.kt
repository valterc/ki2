package com.valterc.ki2.karoo.datatypes

import com.valterc.ki2.data.connection.ConnectionInfo
import com.valterc.ki2.data.connection.ConnectionStatus
import com.valterc.ki2.data.device.BatteryInfo
import com.valterc.ki2.data.device.DeviceId
import com.valterc.ki2.karoo.Ki2ExtensionContext
import io.hammerhead.karooext.extension.DataTypeImpl
import io.hammerhead.karooext.internal.Emitter
import io.hammerhead.karooext.models.DataPoint
import io.hammerhead.karooext.models.DataType
import io.hammerhead.karooext.models.StreamState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.concurrent.locks.ReentrantLock
import java.util.function.BiConsumer
import kotlin.concurrent.withLock

class ShiftingBatteryPercentageDataType(
    extension: String,
    private val extensionContext: Ki2ExtensionContext
) : DataTypeImpl(extension, DATA_TYPE) {

    companion object {
        private const val DATA_TYPE = "DATATYPE_SHIFTING_BATTERY_PERCENTAGE"
    }

    /*
    override fun startStream(emitter: Emitter<StreamState>) {
        val job = CoroutineScope(Dispatchers.IO).launch {
            extensionContext.karooSystem.streamData(
                DataType.dataTypeId(
                    extension,
                    Ki2DataType.Type.DI2
                )
            ).transform { streamState ->
                when (streamState) {
                    is StreamState.Streaming -> {
                        streamState.dataPoint.values[Ki2DataType.Field.DI2_BATTERY]?.let { batteryValue ->
                            emit(
                                StreamState.Streaming(
                                    DataPoint(
                                        dataTypeId,
                                        values = mapOf(DataType.Field.SINGLE to batteryValue),
                                    )
                                )
                            )
                        }
                    }
                    else -> emit(streamState)
                }
            }.collect {
                emitter.onNext(it)
            }
        }

        emitter.setCancellable {
            job.cancel()
        }
    }
    */

    private val lock: ReentrantLock = ReentrantLock()
    private var connectionInfo: ConnectionInfo? = null
    private var batteryInfo: BatteryInfo? = null

    override fun startStream(emitter: Emitter<StreamState>) {
        val connectionInfoListener =
            BiConsumer<DeviceId, ConnectionInfo> { _: DeviceId, connectionInfo: ConnectionInfo ->
                lock.withLock {
                    this.connectionInfo = connectionInfo
                    emitDataPoint(emitter)
                }
            }

        val batteryInfoListener =
            BiConsumer<DeviceId, BatteryInfo> { _: DeviceId, batteryInfo: BatteryInfo ->
                lock.withLock {
                    this.batteryInfo = batteryInfo
                    emitDataPoint(emitter)
                }
            }

        val job = CoroutineScope(Dispatchers.IO).launch {
            extensionContext.serviceClient.registerConnectionInfoWeakListener(
                connectionInfoListener
            )
            extensionContext.serviceClient.registerBatteryInfoWeakListener(
                batteryInfoListener
            )

            while (true) {
                lock.withLock {
                    emitDataPoint(emitter)
                }

                delay(5_000)
            }
        }

        emitter.setCancellable {
            job.cancel()
            extensionContext.serviceClient.unregisterConnectionInfoWeakListener(
                connectionInfoListener
            )
            extensionContext.serviceClient.unregisterBatteryInfoWeakListener(
                batteryInfoListener
            )
        }
    }

    private fun emitDataPoint(emitter: Emitter<StreamState>) {
        when (connectionInfo?.connectionStatus) {
            ConnectionStatus.INVALID,
            ConnectionStatus.NEW,
            ConnectionStatus.CONNECTING -> emitter.onNext(
                StreamState.Searching
            )

            ConnectionStatus.ESTABLISHED ->
                batteryInfo.let { batteryInfo ->
                    if (batteryInfo != null) {
                        emitter.onNext(
                            StreamState.Streaming(
                                DataPoint(
                                    dataTypeId,
                                    values = mapOf(DataType.Field.SINGLE to batteryInfo.value.toDouble()),
                                )
                            )
                        )
                    } else {
                        emitter.onNext(
                            StreamState.Searching
                        )
                    }
                }

            else -> emitter.onNext(
                StreamState.NotAvailable
            )
        }
    }
}