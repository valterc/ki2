package com.valterc.ki2.karoo.datatypes

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.widget.RemoteViews
import androidx.glance.appwidget.ExperimentalGlanceRemoteViewsApi
import androidx.glance.appwidget.GlanceRemoteViews
import com.valterc.ki2.R
import com.valterc.ki2.karoo.Ki2ExtensionContext
import com.valterc.ki2.karoo.views.GearsExtensionView
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
import kotlinx.coroutines.launch
import timber.log.Timber


@Suppress("unused")
@OptIn(ExperimentalGlanceRemoteViewsApi::class)
class GearsDataType(
    extension: String,
    private val extensionContext: Ki2ExtensionContext
) : DataTypeImpl(extension, "graphical-gears") {

    private val glance = GlanceRemoteViews()

    override fun startStream(emitter: Emitter<StreamState>) {
        Timber.d("start speed stream")
        val job = CoroutineScope(Dispatchers.IO).launch {
                emitter.onNext(StreamState.Streaming(DataPoint(dataTypeId, mapOf(DataType.Field.SINGLE to 1.0), extension)))
        }
        emitter.setCancellable {
            Timber.d("stop speed stream")
            job.cancel()
        }
    }

    override fun startView(context: Context, config: ViewConfig, emitter: ViewEmitter) {
        Timber.d("Starting speed view with $emitter")



        val gearsView =
            GearsExtensionView(extensionContext)
        val view = gearsView.createView(config)

        view.measure(440, 200)
        view.layout(0, 0, 440, 200)

        val bitmap = Bitmap.createBitmap(
            440,
            200,
            Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bitmap)
        view.draw(canvas);

        emitter.onNext(UpdateGraphicConfig(showHeader = true))

        gearsView.setViewUpdateListener {
            CoroutineScope(Dispatchers.IO).launch {
                bitmap.eraseColor(Color.TRANSPARENT)
                canvas.drawColor(Color.RED)
                view.invalidate()
                view.forceLayout()
                view.draw(canvas)

                Timber.i("[%s, %s] Update view", dataTypeId, emitter)

                val remoteViews = RemoteViews(context.packageName, R.layout.remote_view_image)
                remoteViews.setImageViewBitmap(R.id.widget_image, bitmap)

                emitter.updateView(remoteViews)
            }
        }

        emitter.setCancellable {
            gearsView.setViewUpdateListener(null)
        }
    }

}