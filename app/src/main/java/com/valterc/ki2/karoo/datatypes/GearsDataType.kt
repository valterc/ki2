package com.valterc.ki2.karoo.datatypes

import android.content.Context
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.view.View
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
) : DataTypeImpl(extension, "DATATYPE_GRAPHICAL_GEARS") {

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

        val gearsView = GearsExtensionView(extensionContext)
        val view = gearsView.createView(config)

        val bitmap = Bitmap.createBitmap(
            config.viewSize.first,
            config.viewSize.second,
            Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bitmap)

        bitmap.eraseColor(Color.TRANSPARENT)
        view.forceLayout()
        view.measure(
            View.MeasureSpec.makeMeasureSpec(
                config.viewSize.first,
                View.MeasureSpec.EXACTLY
            ),
            View.MeasureSpec.makeMeasureSpec(
                config.viewSize.second,
                View.MeasureSpec.EXACTLY
            )
        )
        view.layout(view.left, view.top, view.right, view.bottom)
        view.draw(canvas)

        Timber.i("[%s, %s] Update view", dataTypeId, emitter)

        val remoteViews = RemoteViews(context.packageName, R.layout.remote_view_image)
        remoteViews.setImageViewBitmap(R.id.widget_image, bitmap)

        emitter.updateView(remoteViews)
        emitter.onNext(UpdateGraphicConfig(showHeader = false))

        gearsView.setViewUpdateListener {
            CoroutineScope(Dispatchers.Main).launch {
                bitmap.eraseColor(Color.TRANSPARENT)
                view.forceLayout()
                view.measure(
                    View.MeasureSpec.makeMeasureSpec(
                        config.viewSize.first,
                        View.MeasureSpec.EXACTLY
                    ),
                    View.MeasureSpec.makeMeasureSpec(
                        config.viewSize.second,
                        View.MeasureSpec.EXACTLY
                    )
                )
                view.layout(view.left, view.top, view.right, view.bottom)
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