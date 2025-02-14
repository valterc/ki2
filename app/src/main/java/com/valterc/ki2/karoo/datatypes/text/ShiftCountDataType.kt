package com.valterc.ki2.karoo.datatypes.text

import android.content.Context
import androidx.compose.ui.unit.DpSize
import androidx.glance.appwidget.ExperimentalGlanceRemoteViewsApi
import androidx.glance.appwidget.GlanceRemoteViews
import com.valterc.ki2.data.device.DeviceId
import com.valterc.ki2.data.shifting.ShiftingInfo
import com.valterc.ki2.karoo.Ki2ExtensionContext
import com.valterc.ki2.karoo.datatypes.views.TextView
import io.hammerhead.karooext.extension.DataTypeImpl
import io.hammerhead.karooext.internal.ViewEmitter
import io.hammerhead.karooext.models.UpdateGraphicConfig
import io.hammerhead.karooext.models.ViewConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.function.BiConsumer
import kotlin.math.abs

@OptIn(ExperimentalGlanceRemoteViewsApi::class)
class ShiftCountDataType(private val extensionContext: Ki2ExtensionContext) :
    DataTypeImpl(extensionContext.extension, "DATATYPE_SHIFT_COUNT") {

    private val glance = GlanceRemoteViews()
    private var previousShiftingInfo: ShiftingInfo? = null
    private var shiftCount = 0

    override fun startView(context: Context, config: ViewConfig, emitter: ViewEmitter) {
        emitter.onNext(UpdateGraphicConfig(showHeader = true))

        val shiftingInfoConsumer =
            BiConsumer<DeviceId, ShiftingInfo> { _: DeviceId, shiftingInfo: ShiftingInfo ->
                if (previousShiftingInfo == null) {
                    previousShiftingInfo = shiftingInfo;
                    return@BiConsumer
                }

                val lastShiftingInfo = previousShiftingInfo ?: return@BiConsumer

                shiftCount += abs(lastShiftingInfo.frontGear - shiftingInfo.frontGear);
                shiftCount += abs(lastShiftingInfo.rearGear - shiftingInfo.rearGear);

                previousShiftingInfo = shiftingInfo;

                CoroutineScope(Dispatchers.IO).launch {
                    emitViewUpdate(context, config, emitter)
                }
            }

        val startupJob = CoroutineScope(Dispatchers.IO).launch {
            delay(2_000)
            extensionContext.serviceClient.registerShiftingInfoWeakListener(
                shiftingInfoConsumer
            )
        }

        emitter.setCancellable {
            startupJob.cancel()
            extensionContext.serviceClient.unregisterShiftingInfoWeakListener(
                shiftingInfoConsumer
            )
        }
    }

    private suspend fun emitViewUpdate(context: Context, config: ViewConfig, emitter: ViewEmitter) {
        val compositionResult = glance.compose(context, DpSize.Unspecified) {
            TextView(
                shiftCount.toString(),
                dataAlignment = config.alignment,
                fontSize = config.textSize
            )
        }

        emitter.updateView(compositionResult.remoteViews)
    }

}