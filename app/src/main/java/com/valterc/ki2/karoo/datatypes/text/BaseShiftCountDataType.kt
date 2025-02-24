package com.valterc.ki2.karoo.datatypes.text

import android.content.Context
import androidx.compose.ui.unit.DpSize
import androidx.glance.appwidget.ExperimentalGlanceRemoteViewsApi
import androidx.glance.appwidget.GlanceRemoteViews
import com.valterc.ki2.karoo.Ki2ExtensionContext
import com.valterc.ki2.karoo.datatypes.views.TextView
import com.valterc.ki2.karoo.shifting.ShiftCountHandler
import io.hammerhead.karooext.extension.DataTypeImpl
import io.hammerhead.karooext.internal.ViewEmitter
import io.hammerhead.karooext.models.ShowCustomStreamState
import io.hammerhead.karooext.models.UpdateGraphicConfig
import io.hammerhead.karooext.models.ViewConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@OptIn(ExperimentalGlanceRemoteViewsApi::class)
abstract class BaseShiftCountDataType(private val extensionContext: Ki2ExtensionContext, typeId: String) :
    DataTypeImpl(extensionContext.extension, typeId) {

    private val glance = GlanceRemoteViews()

    override fun startView(context: Context, config: ViewConfig, emitter: ViewEmitter) {
        emitter.onNext(UpdateGraphicConfig(showHeader = true))
        emitter.onNext(ShowCustomStreamState(message = "", color = null))

        val job = CoroutineScope(Dispatchers.IO).launch {
            emitViewUpdate(context, config, emitter, 0)
            extensionContext.shiftCountHandler.stream().collect {
                emitViewUpdate(context, config, emitter, getShiftCountValue(it))
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

    protected abstract fun getShiftCountValue(shiftCountHandler: ShiftCountHandler) : Int

}