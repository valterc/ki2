package com.valterc.ki2.karoo.datatypes.text

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.DpSize
import androidx.glance.appwidget.ExperimentalGlanceRemoteViewsApi
import androidx.glance.appwidget.GlanceRemoteViews
import com.valterc.ki2.R
import com.valterc.ki2.data.connection.ConnectionInfo
import com.valterc.ki2.data.device.DeviceId
import com.valterc.ki2.data.switches.WirelessSwitchBatteryLevel
import com.valterc.ki2.data.switches.WirelessSwitchesInfo
import com.valterc.ki2.karoo.Ki2ExtensionContext
import com.valterc.ki2.karoo.datatypes.ThrottledViewEmitter
import com.valterc.ki2.karoo.datatypes.views.NotAvailable
import com.valterc.ki2.karoo.datatypes.views.TextView
import com.valterc.ki2.karoo.datatypes.views.Waiting
import io.hammerhead.karooext.extension.DataTypeImpl
import io.hammerhead.karooext.internal.ViewEmitter
import io.hammerhead.karooext.models.ShowCustomStreamState
import io.hammerhead.karooext.models.UpdateGraphicConfig
import io.hammerhead.karooext.models.ViewConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.function.BiConsumer

@OptIn(ExperimentalGlanceRemoteViewsApi::class)
class SwitchBatteryDataType(
    private val extensionContext: Ki2ExtensionContext
) : DataTypeImpl(extensionContext.extension, "DATATYPE_SWITCH_BATTERY") {

    private data class ViewState(
        val connectionDeviceId: DeviceId? = null,
        val connectionInfo: ConnectionInfo? = null,
        val switchesDeviceId: DeviceId? = null,
        val switchesInfo: WirelessSwitchesInfo? = null
    )

    override fun startView(context: Context, config: ViewConfig, emitter: ViewEmitter) {
        val emitter = ThrottledViewEmitter(emitter)
        val glance = GlanceRemoteViews()
        val state = MutableStateFlow(ViewState())

        emitter.onNext(UpdateGraphicConfig(showHeader = true))
        emitter.onNext(ShowCustomStreamState(message = "", color = null))

        val connectionInfoListener = BiConsumer<DeviceId, ConnectionInfo> { deviceId, info ->
            state.update { it.copy(connectionDeviceId = deviceId, connectionInfo = info) }
        }
        val switchesListener = BiConsumer<DeviceId, WirelessSwitchesInfo> { deviceId, info ->
            state.update { it.copy(switchesDeviceId = deviceId, switchesInfo = info) }
        }

        val job = CoroutineScope(Dispatchers.IO).launch {
            state.collectLatest { current ->
                val info = current.switchesInfo?.takeIf {
                    current.switchesDeviceId == current.connectionDeviceId
                }
                val compositionResult = glance.compose(context, DpSize.Unspecified) {
                    when {
                        current.connectionInfo?.isClosed == true ->
                            NotAvailable(dataAlignment = config.alignment, transparentBackground = true)

                        current.connectionInfo?.isConnected != true || info == null ->
                            Waiting(dataAlignment = config.alignment, transparentBackground = true)

                        info.switches.isEmpty() ->
                            NotAvailable(dataAlignment = config.alignment, transparentBackground = true)

                        else -> BatteryContent(context, config, info.lowestBatteryLevel)
                    }
                }
                emitter.updateView(compositionResult.remoteViews)
            }
        }

        extensionContext.serviceClient.registerConnectionInfoWeakListener(connectionInfoListener)
        extensionContext.serviceClient.registerWirelessSwitchesWeakListener(switchesListener)

        emitter.setCancellable {
            job.cancel()
            extensionContext.serviceClient.unregisterConnectionInfoWeakListener(connectionInfoListener)
            extensionContext.serviceClient.unregisterWirelessSwitchesWeakListener(switchesListener)
        }
    }

    @Composable
    private fun BatteryContent(
        context: Context,
        config: ViewConfig,
        level: WirelessSwitchBatteryLevel
    ) {
        TextView(
            text = batteryLevelText(context, level),
            dataAlignment = config.alignment,
            fontSize = if (level == WirelessSwitchBatteryLevel.UNKNOWN) 20 else config.textSize,
            transparentBackground = true
        )
    }

    private fun batteryLevelText(context: Context, level: WirelessSwitchBatteryLevel): String {
        return context.getString(
            when (level) {
                WirelessSwitchBatteryLevel.LOW -> R.string.text_battery_low
                WirelessSwitchBatteryLevel.MID -> R.string.text_battery_mid
                WirelessSwitchBatteryLevel.HIGH -> R.string.text_battery_high
                WirelessSwitchBatteryLevel.UNKNOWN -> R.string.text_unknown
            }
        )
    }
}
