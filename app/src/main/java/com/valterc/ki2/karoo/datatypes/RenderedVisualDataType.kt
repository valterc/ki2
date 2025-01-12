package com.valterc.ki2.karoo.datatypes

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.view.View
import android.widget.RemoteViews
import androidx.glance.appwidget.ExperimentalGlanceRemoteViewsApi
import androidx.glance.appwidget.GlanceRemoteViews
import com.valterc.ki2.R
import com.valterc.ki2.karoo.Ki2ExtensionContext
import com.valterc.ki2.karoo.views.Ki2ExtensionView
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


@Suppress("unused")
@OptIn(ExperimentalGlanceRemoteViewsApi::class)
open class RenderedVisualDataType(
    dataTypeId: String,
    private val extensionContext: Ki2ExtensionContext,
    private val extensionViewProvider: (Ki2ExtensionContext) -> Ki2ExtensionView
) : DataTypeImpl(extensionContext.extension, dataTypeId) {

    private val glance = GlanceRemoteViews()

    override fun startStream(emitter: Emitter<StreamState>) {
        val job = CoroutineScope(Dispatchers.IO).launch {
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
        emitter.setCancellable {
            job.cancel()
        }
    }

    override fun startView(context: Context, config: ViewConfig, emitter: ViewEmitter) {
        val extensionView = extensionViewProvider(extensionContext)
        val view = extensionView.createView(config)
        val remoteViews = RemoteViews(context.packageName, R.layout.remote_view_image)

        val bitmap = Bitmap.createBitmap(
            config.viewSize.first,
            config.viewSize.second,
            Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bitmap)

        emitter.onNext(UpdateGraphicConfig(showHeader = false))

        renderView(bitmap, view, config, canvas)
        remoteViews.setImageViewBitmap(R.id.widget_image, bitmap)
        emitter.updateView(remoteViews)

        extensionView.setViewUpdateListener {
            CoroutineScope(Dispatchers.Main).launch {
                renderView(bitmap, view, config, canvas)
                remoteViews.setImageViewBitmap(R.id.widget_image, bitmap)
                emitter.updateView(remoteViews)
            }
        }

        emitter.setCancellable {
            extensionView.dispose()
        }
    }

    private fun renderView(
        bitmap: Bitmap,
        view: View,
        config: ViewConfig,
        canvas: Canvas
    ) {
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
    }

}