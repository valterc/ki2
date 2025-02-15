package com.valterc.ki2.karoo.datatypes.text

import android.content.Context
import androidx.compose.ui.unit.DpSize
import androidx.glance.appwidget.ExperimentalGlanceRemoteViewsApi
import androidx.glance.appwidget.GlanceRemoteViews
import com.valterc.ki2.karoo.Ki2ExtensionContext
import com.valterc.ki2.karoo.datatypes.views.TextView
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

@OptIn(ExperimentalGlanceRemoteViewsApi::class)
class RearShiftCountDataType(private val extensionContext: Ki2ExtensionContext) :
    DataTypeImpl(extensionContext.extension, "DATATYPE_REAR_SHIFT_COUNT") {

    private val glance = GlanceRemoteViews()

    override fun startStream(emitter: Emitter<StreamState>) {
        emitter.onNext(
            StreamState.Streaming(
                DataPoint(
                    dataTypeId,
                    mapOf(DataType.Field.SINGLE to 1.0),
                    extension
                )
            )
        )
    }

    override fun startView(context: Context, config: ViewConfig, emitter: ViewEmitter) {
        emitter.onNext(UpdateGraphicConfig(showHeader = true))

        val job = CoroutineScope(Dispatchers.IO).launch {
            delay(2_000)
            emitViewUpdate(context, config, emitter, 0)
            extensionContext.shiftCountHandler.stream().collect {
                emitViewUpdate(context, config, emitter, it.rearShiftCount)
            }
        }

        emitter.setCancellable {
            job.cancel()
        }
    }

    private suspend fun emitViewUpdate(context: Context, config: ViewConfig, emitter: ViewEmitter, shiftCount: Int) {
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