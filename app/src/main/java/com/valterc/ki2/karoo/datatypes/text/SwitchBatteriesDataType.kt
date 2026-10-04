package com.valterc.ki2.karoo.datatypes.text

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.util.TypedValue
import android.view.View
import androidx.compose.ui.unit.DpSize
import androidx.core.graphics.createBitmap
import androidx.glance.appwidget.ExperimentalGlanceRemoteViewsApi
import androidx.glance.appwidget.GlanceRemoteViews
import com.valterc.ki2.R
import com.valterc.ki2.data.connection.ConnectionInfo
import com.valterc.ki2.data.device.DeviceId
import com.valterc.ki2.data.switches.SwitchSide
import com.valterc.ki2.data.switches.WirelessSwitchBatteryLevel
import com.valterc.ki2.data.switches.WirelessSwitchName
import com.valterc.ki2.data.switches.WirelessSwitchesInfo
import com.valterc.ki2.karoo.Ki2ExtensionContext
import com.valterc.ki2.karoo.datatypes.ThrottledViewEmitter
import com.valterc.ki2.karoo.datatypes.views.SwitchBatteries
import com.valterc.ki2.karoo.datatypes.views.SwitchBatteriesMessage
import com.valterc.ki2.karoo.datatypes.views.SwitchBatteryItem
import com.valterc.ki2.karoo.datatypes.views.SwitchBatteryRow
import com.valterc.ki2.views.battery.BatteryIndicator
import com.valterc.ki2.views.battery.BatteryView
import io.hammerhead.karooext.extension.DataTypeImpl
import io.hammerhead.karooext.internal.ViewEmitter
import io.hammerhead.karooext.models.ShowCustomStreamState
import io.hammerhead.karooext.models.UpdateGraphicConfig
import io.hammerhead.karooext.models.ViewConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import java.util.function.BiConsumer
import kotlin.math.ceil

@OptIn(ExperimentalGlanceRemoteViewsApi::class)
class SwitchBatteriesDataType(private val extensionContext: Ki2ExtensionContext) :
    DataTypeImpl(extensionContext.extension, "DATATYPE_SWITCH_BATTERIES") {

    private val glance = GlanceRemoteViews()
    private val batteryBitmaps = mutableMapOf<Pair<WirelessSwitchBatteryLevel, Boolean>, Bitmap>()

    private data class ViewState(
        val connectionDeviceId: DeviceId? = null,
        val connectionInfo: ConnectionInfo? = null,
        val wirelessSwitchesDeviceId: DeviceId? = null,
        val wirelessSwitchesInfo: WirelessSwitchesInfo? = null
    )

    override fun startView(context: Context, config: ViewConfig, emitter: ViewEmitter) {
        val emitter = ThrottledViewEmitter(emitter)

        emitter.onNext(UpdateGraphicConfig(showHeader = true))
        emitter.onNext(ShowCustomStreamState(message = "", color = null))

        val stateLock = Any()
        var state = ViewState()
        val states = Channel<ViewState>(Channel.CONFLATED)

        fun updateState(transform: (ViewState) -> ViewState) {
            synchronized(stateLock) {
                state = transform(state)
                states.trySend(state)
            }
        }

        val job = CoroutineScope(Dispatchers.IO).launch {
            for (viewState in states) {
                emitViewUpdate(context, config, emitter, viewState)
            }
        }

        val connectionInfoListener =
            BiConsumer<DeviceId, ConnectionInfo> { deviceId: DeviceId, connectionInfo: ConnectionInfo ->
                updateState { it.copy(connectionDeviceId = deviceId, connectionInfo = connectionInfo) }
            }

        val wirelessSwitchesListener =
            BiConsumer<DeviceId, WirelessSwitchesInfo> { deviceId: DeviceId, wirelessSwitchesInfo: WirelessSwitchesInfo ->
                updateState { it.copy(wirelessSwitchesDeviceId = deviceId, wirelessSwitchesInfo = wirelessSwitchesInfo) }
            }

        extensionContext.serviceClient.registerConnectionInfoWeakListener(
            connectionInfoListener
        )
        extensionContext.serviceClient.registerWirelessSwitchesWeakListener(
            wirelessSwitchesListener
        )

        emitter.setCancellable {
            job.cancel()
            states.close()
            extensionContext.serviceClient.unregisterConnectionInfoWeakListener(
                connectionInfoListener
            )
            extensionContext.serviceClient.unregisterWirelessSwitchesWeakListener(
                wirelessSwitchesListener
            )
        }
    }

    private suspend fun emitViewUpdate(context: Context, config: ViewConfig, emitter: ThrottledViewEmitter, state: ViewState) {
        val connectionInfo = state.connectionInfo
        val wirelessSwitchesInfo = state.wirelessSwitchesInfo?.takeIf { state.wirelessSwitchesDeviceId == state.connectionDeviceId }

        val compositionResult =
            if (connectionInfo?.isConnected == true && wirelessSwitchesInfo != null) {
                if (wirelessSwitchesInfo.switches.isEmpty()) {
                    val text = context.getString(R.string.text_no_switches)
                    glance.compose(context, DpSize.Unspecified) {
                        SwitchBatteriesMessage(text, dataAlignment = config.alignment, fontSize = getMessageFontSize(context, config))
                    }
                } else {
                    val rows = getRows(context, wirelessSwitchesInfo)
                    val typeNameWidthDp = getTypeNameWidthDp(context, rows)
                    glance.compose(context, DpSize.Unspecified) {
                        SwitchBatteries(rows, typeNameWidthDp, TYPE_NAME_FONT_SIZE)
                    }
                }
            } else {
                val text = context.getString(R.string.text_waiting_for_data)
                glance.compose(context, DpSize.Unspecified) {
                    SwitchBatteriesMessage(text, dataAlignment = config.alignment, fontSize = getMessageFontSize(context, config))
                }
            }

        emitter.updateView(compositionResult.remoteViews)
    }

    private fun getMessageFontSize(context: Context, config: ViewConfig): Int {
        val screenWidth = context.resources.displayMetrics.widthPixels
        return if (config.viewSize.first < screenWidth * NARROW_FIELD_WIDTH_RATIO) MESSAGE_FONT_SIZE_NARROW_FIELD else MESSAGE_FONT_SIZE_WIDE_FIELD
    }

    private fun getRows(context: Context, wirelessSwitchesInfo: WirelessSwitchesInfo): List<SwitchBatteryRow> {
        val switchesByType = wirelessSwitchesInfo.switches
            .sortedBy { it.slot }
            .groupBy { it.type }
            .toSortedMap()
        val showType = switchesByType.size > 1

        return switchesByType.values.map { switches ->
            SwitchBatteryRow(
                if (showType) WirelessSwitchName.getShortTypeName(context, switches.first()).uppercase() else null,
                switches.map {
                    SwitchBatteryItem(
                        when (it.side) {
                            SwitchSide.LEFT, SwitchSide.RIGHT -> it.side
                            else -> SwitchSide.NONE
                        },
                        getBatteryBitmap(context, it.batteryLevel, !showType)
                    )
                }
            )
        }
    }

    private fun getTypeNameWidthDp(context: Context, rows: List<SwitchBatteryRow>): Int {
        val paint = Paint()
        paint.textSize = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_SP,
            TYPE_NAME_FONT_SIZE.toFloat(),
            context.resources.displayMetrics
        )

        val widthPx = rows.maxOfOrNull { row -> row.typeName?.let { paint.measureText(it) } ?: 0f } ?: 0f
        return ceil(widthPx / context.resources.displayMetrics.density).toInt() + TYPE_NAME_MARGIN_DP
    }

    private fun getBatteryBitmap(
        context: Context,
        batteryLevel: WirelessSwitchBatteryLevel,
        singleRow: Boolean
    ): Bitmap {
        return synchronized(batteryBitmaps) {
            batteryBitmaps.getOrPut(Pair(batteryLevel, singleRow)) {
                createBatteryBitmap(context, batteryLevel, singleRow)
            }
        }
    }

    private fun createBatteryBitmap(
        context: Context,
        batteryLevel: WirelessSwitchBatteryLevel,
        singleRow: Boolean
    ): Bitmap {
        val batteryView = BatteryView(context, null)
        batteryView.orientation = BatteryView.Orientation.HORIZONTAL_RIGHT
        batteryView.borderStrokeWidth = BATTERY_BORDER_WIDTH
        batteryView.value = when (batteryLevel) {
            WirelessSwitchBatteryLevel.HIGH -> 1f
            WirelessSwitchBatteryLevel.MID -> 0.5f
            WirelessSwitchBatteryLevel.LOW -> 0.15f
            else -> 0f
        }

        val color = context.getColor(
            BatteryIndicator.fromWirelessSwitchBatteryLevel(batteryLevel)?.colorId ?: R.color.hh_grey
        )
        batteryView.setForegroundColor(color)
        batteryView.setBorderColor(color)

        batteryView.measure(
            View.MeasureSpec.makeMeasureSpec(BATTERY_WIDTH, View.MeasureSpec.EXACTLY),
            View.MeasureSpec.makeMeasureSpec(BATTERY_HEIGHT, View.MeasureSpec.EXACTLY)
        )
        batteryView.layout(0, 0, BATTERY_WIDTH, BATTERY_HEIGHT)

        val bitmapHeight = if (singleRow) BATTERY_HEIGHT * 2 else BATTERY_HEIGHT
        val bitmap = createBitmap(BATTERY_WIDTH, bitmapHeight)
        val canvas = Canvas(bitmap)
        canvas.translate(0f, (bitmapHeight - BATTERY_HEIGHT) / 2f)
        batteryView.draw(canvas)
        return bitmap
    }

    companion object {
        private const val TYPE_NAME_FONT_SIZE = 13
        private const val NARROW_FIELD_WIDTH_RATIO = 0.75
        private const val MESSAGE_FONT_SIZE_WIDE_FIELD = 18
        private const val MESSAGE_FONT_SIZE_NARROW_FIELD = 14
        private const val TYPE_NAME_MARGIN_DP = 6
        private const val BATTERY_WIDTH = 120
        private const val BATTERY_HEIGHT = 60
        private const val BATTERY_BORDER_WIDTH = 6f
    }
}
