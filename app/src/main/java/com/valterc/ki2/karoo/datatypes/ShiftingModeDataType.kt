package com.valterc.ki2.karoo.datatypes

import android.content.Context
import androidx.compose.ui.unit.DpSize
import androidx.glance.appwidget.ExperimentalGlanceRemoteViewsApi
import androidx.glance.appwidget.GlanceRemoteViews
import com.valterc.ki2.data.shifting.ShiftingMode
import com.valterc.ki2.karoo.Ki2ExtensionContext
import com.valterc.ki2.karoo.streamData
import io.hammerhead.karooext.extension.DataTypeImpl
import io.hammerhead.karooext.internal.ViewEmitter
import io.hammerhead.karooext.models.StreamState
import io.hammerhead.karooext.models.UpdateGraphicConfig
import io.hammerhead.karooext.models.ViewConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.launch

@OptIn(ExperimentalGlanceRemoteViewsApi::class)
class ShiftingModeDataType(extension: String, private val extensionContext: Ki2ExtensionContext) :
    DataTypeImpl(extension, TYPE_ID) {

    companion object {
        const val TYPE_ID = "TYPE_SHIFTING_BATTERY_PERCENTAGE"
    }

    private val glance = GlanceRemoteViews()

    override fun startView(context: Context, config: ViewConfig, emitter: ViewEmitter) {
        emitter.onNext(UpdateGraphicConfig(showHeader = true))

        val flow = extensionContext.karooSystem.streamData(dataTypeId)
            .onCompletion {
                val result = glance.compose(context, DpSize.Unspecified) { }
                emitter.updateView(result.remoteViews)
            }

        val viewJob = CoroutineScope(Dispatchers.IO).launch {
            flow.collect { streamState ->

                val compositionResult = when (streamState) {
                    is StreamState.Streaming -> {
                        glance.compose(context, DpSize.Unspecified) {
                            TextView(
                                ShiftingMode.fromValue(
                                    streamState.dataPoint.singleValue?.toInt()
                                        ?: ShiftingMode.INVALID.value
                                ).mode, config.textSize
                            )
                        }
                    }

                    is StreamState.Idle -> {
                        glance.compose(context, DpSize.Unspecified) {
                            TextView(null, config.textSize)
                        }
                    }

                    is StreamState.Searching -> {
                        glance.compose(context, DpSize.Unspecified) {
                            TextView("...", config.textSize)
                        }
                    }

                    is StreamState.NotAvailable -> {
                        glance.compose(context, DpSize.Unspecified) {
                            TextView("N/A", config.textSize)
                        }
                    }
                }

                emitter.updateView(compositionResult.remoteViews)
            }
        }

        emitter.setCancellable {
            viewJob.cancel()
        }
    }
}