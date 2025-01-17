package com.valterc.ki2.karoo.datatypes

import android.content.Context
import androidx.compose.ui.unit.DpSize
import androidx.glance.appwidget.ExperimentalGlanceRemoteViewsApi
import androidx.glance.appwidget.GlanceRemoteViews
import com.valterc.ki2.data.connection.ConnectionInfo
import com.valterc.ki2.data.connection.ConnectionStatus
import com.valterc.ki2.data.device.DeviceId
import com.valterc.ki2.data.shifting.ShiftingInfo
import com.valterc.ki2.data.shifting.ShiftingMode
import com.valterc.ki2.karoo.Ki2ExtensionContext
import com.valterc.ki2.karoo.datatypes.views.TextView
import com.valterc.ki2.karoo.streamData
import io.hammerhead.karooext.extension.DataTypeImpl
import io.hammerhead.karooext.internal.Emitter
import io.hammerhead.karooext.internal.ViewEmitter
import io.hammerhead.karooext.models.DataPoint
import io.hammerhead.karooext.models.DataType
import io.hammerhead.karooext.models.StreamState
import io.hammerhead.karooext.models.UpdateGraphicConfig
import io.hammerhead.karooext.models.ViewConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.concurrent.locks.ReentrantLock
import java.util.function.BiConsumer
import kotlin.concurrent.withLock

@OptIn(ExperimentalGlanceRemoteViewsApi::class)
class ShiftingModeDataType(private val extensionContext: Ki2ExtensionContext) :
    DataTypeImpl(extensionContext.extension, "DATATYPE_SHIFTING_MODE") {

    private val glance = GlanceRemoteViews()

    /*
    override fun startView(context: Context, config: ViewConfig, emitter: ViewEmitter) {
        emitter.onNext(UpdateGraphicConfig(showHeader = true))

        val flow = extensionContext.karooSystem.streamData(
            DataType.dataTypeId(
                extension,
                Ki2DataType.Type.DI2
            )
        )

        val viewJob = CoroutineScope(Dispatchers.IO).launch {
            flow.collect { streamState ->
                Timber.i("collect: %s", streamState)

                val compositionResult = when (streamState) {
                    is StreamState.Streaming -> {
                        streamState.dataPoint.values[Ki2DataType.Field.DI2_SHIFTING_MODE]?.let { shiftingModeValue ->
                            glance.compose(context, DpSize.Unspecified) {
                                TextView(
                                    ShiftingMode.fromValue(shiftingModeValue.toInt()).mode,
                                    config.alignment,
                                    config.textSize
                                )
                            }
                        }
                    }

                    else -> {
                        glance.compose(context, DpSize.Unspecified) {
                        }
                    }
                }

                compositionResult?.let {
                    emitter.updateView(compositionResult.remoteViews)
                }
            }
        }

        emitter.setCancellable {
            viewJob.cancel()
        }
    }
    */

    private val lock: ReentrantLock = ReentrantLock()
    private var connectionInfo: ConnectionInfo? = null
    private var shiftingInfo: ShiftingInfo? = null

    override fun startStream(emitter: Emitter<StreamState>) {
        val connectionInfoListener =
            BiConsumer<DeviceId, ConnectionInfo> { _: DeviceId, connectionInfo: ConnectionInfo ->
                lock.withLock {
                    this.connectionInfo = connectionInfo
                    emitDataPoint(emitter)
                }
            }

        val shiftingInfoConsumer =
            BiConsumer<DeviceId, ShiftingInfo> { _: DeviceId, shiftingInfo: ShiftingInfo ->
                lock.withLock {
                    this.shiftingInfo = shiftingInfo
                    emitDataPoint(emitter)
                }
            }

        val job = CoroutineScope(Dispatchers.IO).launch {
            extensionContext.serviceClient.registerConnectionInfoWeakListener(
                connectionInfoListener
            )
            extensionContext.serviceClient.registerShiftingInfoWeakListener(
                shiftingInfoConsumer
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
            extensionContext.serviceClient.unregisterShiftingInfoWeakListener(
                shiftingInfoConsumer
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
                shiftingInfo.let { shiftingInfo ->
                    if (shiftingInfo != null) {
                        emitter.onNext(
                            StreamState.Streaming(
                                DataPoint(
                                    dataTypeId,
                                    values = mapOf(DataType.Field.SINGLE to shiftingInfo.shiftingMode.value.toDouble()),
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

    override fun startView(context: Context, config: ViewConfig, emitter: ViewEmitter) {
        emitter.onNext(UpdateGraphicConfig(showHeader = true))

        val flow = extensionContext.karooSystem.streamData(
            dataTypeId
        )

        val viewJob = CoroutineScope(Dispatchers.IO).launch {
            flow.collect { streamState ->
                val compositionResult = when (streamState) {
                    is StreamState.Streaming -> {
                        streamState.dataPoint.singleValue?.let { shiftingModeValue ->
                            glance.compose(context, DpSize.Unspecified) {
                                TextView(
                                    ShiftingMode.fromValue(shiftingModeValue.toInt()).mode,
                                    config.alignment,
                                    config.textSize
                                )
                            }
                        }
                    }

                    else -> {
                        glance.compose(context, DpSize.Unspecified) {
                        }
                    }
                }

                compositionResult?.let {
                    emitter.updateView(compositionResult.remoteViews)
                }
            }
        }

        emitter.setCancellable {
            viewJob.cancel()
        }
    }

}