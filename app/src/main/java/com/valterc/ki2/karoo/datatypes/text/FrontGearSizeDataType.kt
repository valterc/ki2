package com.valterc.ki2.karoo.datatypes.text

import android.content.Context
import androidx.compose.ui.unit.DpSize
import androidx.glance.appwidget.ExperimentalGlanceRemoteViewsApi
import androidx.glance.appwidget.GlanceRemoteViews
import com.valterc.ki2.data.connection.ConnectionInfo
import com.valterc.ki2.data.device.DeviceId
import com.valterc.ki2.data.preferences.device.DevicePreferencesView
import com.valterc.ki2.data.shifting.ShiftingInfo
import com.valterc.ki2.karoo.Ki2ExtensionContext
import com.valterc.ki2.karoo.datatypes.views.NotAvailable
import com.valterc.ki2.karoo.datatypes.views.TextView
import com.valterc.ki2.karoo.datatypes.views.Waiting
import com.valterc.ki2.karoo.shifting.ShiftingGearingHelper
import io.hammerhead.karooext.extension.DataTypeImpl
import io.hammerhead.karooext.internal.ViewEmitter
import io.hammerhead.karooext.models.ShowCustomStreamState
import io.hammerhead.karooext.models.UpdateGraphicConfig
import io.hammerhead.karooext.models.ViewConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.function.BiConsumer

@OptIn(ExperimentalGlanceRemoteViewsApi::class)
class FrontGearSizeDataType(private val extensionContext: Ki2ExtensionContext) :
    DataTypeImpl(extensionContext.extension, "DATATYPE_FRONT_GEAR_SIZE") {

    private val glance = GlanceRemoteViews()
    private var connectionInfo: ConnectionInfo? = null
    private var shiftingGearingHelper = ShiftingGearingHelper(extensionContext.context)

    override fun startView(context: Context, config: ViewConfig, emitter: ViewEmitter) {
        emitter.onNext(UpdateGraphicConfig(showHeader = true))
        emitter.onNext(ShowCustomStreamState(message = "", color = null))

        val connectionInfoListener =
            BiConsumer<DeviceId, ConnectionInfo> { _: DeviceId, connectionInfo: ConnectionInfo ->
                this.connectionInfo = connectionInfo
                CoroutineScope(Dispatchers.IO).launch {
                    emitViewUpdate(context, config, emitter)
                }
            }

        val devicePreferencesConsumer =
            BiConsumer<DeviceId, DevicePreferencesView> { _: DeviceId, devicePreferences: DevicePreferencesView ->
                shiftingGearingHelper.setDevicePreferences(devicePreferences)
                CoroutineScope(Dispatchers.IO).launch {
                    emitViewUpdate(context, config, emitter)
                }
            }

        val shiftingInfoConsumer =
            BiConsumer<DeviceId, ShiftingInfo> { _: DeviceId, shiftingInfo: ShiftingInfo ->
                shiftingGearingHelper.setShiftingInfo(shiftingInfo)
                CoroutineScope(Dispatchers.IO).launch {
                    emitViewUpdate(context, config, emitter)
                }
            }

        val startupJob = CoroutineScope(Dispatchers.IO).launch {
            extensionContext.serviceClient.registerConnectionInfoWeakListener(
                connectionInfoListener
            )
            extensionContext.serviceClient.registerDevicePreferencesWeakListener(
                devicePreferencesConsumer
            )
            extensionContext.serviceClient.registerShiftingInfoWeakListener(
                shiftingInfoConsumer
            )
        }

        emitter.setCancellable {
            startupJob.cancel()

            extensionContext.serviceClient.unregisterConnectionInfoWeakListener(
                connectionInfoListener
            )
            extensionContext.serviceClient.unregisterDevicePreferencesWeakListener(
                devicePreferencesConsumer
            )
            extensionContext.serviceClient.unregisterShiftingInfoWeakListener(
                shiftingInfoConsumer
            )
        }
    }

    private suspend fun emitViewUpdate(context: Context, config: ViewConfig, emitter: ViewEmitter) {
        val compositionResult =
            if (connectionInfo?.isConnected == true && shiftingGearingHelper.hasValidGearingInfo()) {
                glance.compose(context, DpSize.Unspecified) {
                    TextView(shiftingGearingHelper.frontGearTeethCount.toString(),
                        dataAlignment = config.alignment,
                        fontSize = config.textSize
                    )
                }
            } else if (connectionInfo?.isClosed == true) {
                glance.compose(context, DpSize.Unspecified) {
                    NotAvailable(dataAlignment = config.alignment)
                }
            } else {
                glance.compose(context, DpSize.Unspecified) {
                    Waiting(dataAlignment = config.alignment)
                }
            }

        compositionResult.let {
            emitter.updateView(compositionResult.remoteViews)
        }
    }

}