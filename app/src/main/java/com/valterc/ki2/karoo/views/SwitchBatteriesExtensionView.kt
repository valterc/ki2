package com.valterc.ki2.karoo.views

import android.annotation.SuppressLint
import android.text.Layout
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import com.valterc.ki2.R
import com.valterc.ki2.data.connection.ConnectionInfo
import com.valterc.ki2.data.device.DeviceId
import com.valterc.ki2.data.switches.WirelessSwitchBatteryLevel
import com.valterc.ki2.data.switches.WirelessSwitchesInfo
import com.valterc.ki2.karoo.Ki2ExtensionContext
import com.valterc.ki2.views.battery.BatteryView
import com.valterc.ki2.views.battery.SwitchBatteriesView
import io.hammerhead.karooext.models.ViewConfig
import java.util.function.BiConsumer
import kotlin.math.ceil
import kotlin.math.min

class SwitchBatteriesExtensionView(
    context: Ki2ExtensionContext,
    private val showLowestBatteryOnly: Boolean = false
) : Ki2ExtensionView(context) {
    private var connectionDeviceId: DeviceId? = null
    private var connectionInfo: ConnectionInfo? = null
    private var switchesDeviceId: DeviceId? = null
    private var switchesInfo: WirelessSwitchesInfo? = null
    private var rootView: View? = null
    private var viewConfig: ViewConfig? = null
    private var batteriesView: SwitchBatteriesView? = null
    private var batteryView: BatteryView? = null
    private var messageView: TextView? = null

    private val connectionInfoConsumer = BiConsumer<DeviceId, ConnectionInfo> { deviceId, info ->
        connectionDeviceId = deviceId
        connectionInfo = info
        updateView()
    }

    private val switchesConsumer = BiConsumer<DeviceId, WirelessSwitchesInfo> { deviceId, info ->
        switchesDeviceId = deviceId
        switchesInfo = info
        updateView()
    }

    init {
        serviceClient.registerConnectionInfoWeakListener(connectionInfoConsumer)
        serviceClient.registerWirelessSwitchesWeakListener(switchesConsumer)
    }

    override fun dispose() {
        super.dispose()
        serviceClient.unregisterConnectionInfoWeakListener(connectionInfoConsumer)
        serviceClient.unregisterWirelessSwitchesWeakListener(switchesConsumer)
    }

    @SuppressLint("InflateParams")
    override fun createView(layoutInflater: LayoutInflater, viewConfig: ViewConfig): View {
        val view = layoutInflater.inflate(R.layout.view_karoo_switch_batteries, null)
        rootView = view
        this.viewConfig = viewConfig
        batteriesView = view.findViewById(R.id.switchbatteriesview_karoo)
        batteryView = view.findViewById(R.id.batteryview_karoo_switch_battery)
        val textColor = context.getColor(if (karooTheme == KarooTheme.WHITE) R.color.hh_black else R.color.white)
        view.findViewById<TextView>(R.id.textview_karoo_switch_batteries_header).apply {
            setTextColor(textColor)
            setText(if (showLowestBatteryOnly) R.string.text_switch_battery else R.string.text_switch_batteries)
        }
        messageView = view.findViewById<TextView>(R.id.textview_karoo_switch_batteries_message).apply {
            setTextColor(textColor)
            gravity = Gravity.CENTER_VERTICAL or when (viewConfig.alignment) {
                ViewConfig.Alignment.LEFT -> Gravity.START
                else -> Gravity.END
            }
        }
        updateView()
        return view
    }

    private fun updateLayout() {
        val view = rootView ?: return
        val viewConfig = viewConfig ?: return
        val header = view.findViewById<TextView>(R.id.textview_karoo_switch_batteries_header)
        if (!showLowestBatteryOnly) {
            header.setText(R.string.text_switch_batteries)
        }
        val widthSpec = View.MeasureSpec.makeMeasureSpec(viewConfig.viewSize.first, View.MeasureSpec.EXACTLY)
        val heightSpec = View.MeasureSpec.makeMeasureSpec(viewConfig.viewSize.second, View.MeasureSpec.EXACTLY)
        view.measure(widthSpec, heightSpec)

        val availableTitleWidth = header.measuredWidth - header.compoundPaddingLeft - header.compoundPaddingRight
        val batteriesView = batteriesView
        if (!showLowestBatteryOnly && batteriesView?.visibility == View.VISIBLE &&
            batteriesView.rowCount > 1 && titleWidth(header) > availableTitleWidth) {
            header.setText(R.string.text_short_switch_batteries)
        }

        view.measure(widthSpec, heightSpec)
        if (showLowestBatteryOnly) {
            val content = view.findViewById<View>(R.id.framelayout_karoo_switch_batteries_content)
            batteryView?.apply {
                val batteryWidth = min(
                    content.measuredWidth - content.paddingLeft - content.paddingRight,
                    (content.measuredHeight - content.paddingTop - content.paddingBottom) / 2
                ).coerceAtLeast(0)
                layoutParams = layoutParams.apply {
                    width = batteryWidth
                    height = batteryWidth * 2
                }
                borderStrokeWidth = batteryWidth * 0.1f
            }
            view.measure(widthSpec, heightSpec)
        }
        view.layout(0, 0, viewConfig.viewSize.first, viewConfig.viewSize.second)
    }

    private fun titleWidth(header: TextView): Float {
        val text = header.transformationMethod?.getTransformation(header.text, header) ?: header.text
        return ceil(Layout.getDesiredWidth(text, header.paint))
    }

    private fun updateView() {
        val batteriesView = batteriesView ?: return
        val batteryView = batteryView ?: return
        val messageView = messageView ?: return
        val viewConfig = viewConfig ?: return
        val info = switchesInfo?.takeIf { switchesDeviceId == connectionDeviceId }

        if (connectionInfo?.isConnected != true || info == null) {
            batteriesView.visibility = View.INVISIBLE
            batteryView.visibility = View.INVISIBLE
            messageView.visibility = View.VISIBLE
            messageView.setText(
                when {
                    connectionInfo?.isClosed == true -> R.string.text_na
                    // Karoo uses 60 grid columns for full width, independently of screen resolution.
                    viewConfig.gridSize.first < 60 -> R.string.text_short_waiting_for_data
                    else -> R.string.text_waiting_for_data
                }
            )
        } else if (info.switches.isEmpty()) {
            batteriesView.visibility = View.INVISIBLE
            batteryView.visibility = View.INVISIBLE
            messageView.visibility = View.VISIBLE
            messageView.setText(R.string.text_no_switches)
        } else if (showLowestBatteryOnly) {
            val level = info.lowestBatteryLevel
            batteryView.value = when (level) {
                WirelessSwitchBatteryLevel.HIGH -> 1f
                WirelessSwitchBatteryLevel.MID -> 0.5f
                WirelessSwitchBatteryLevel.LOW -> 0.15f
                WirelessSwitchBatteryLevel.UNKNOWN -> 0f
            }
            val color = context.getColor(when (level) {
                WirelessSwitchBatteryLevel.HIGH, WirelessSwitchBatteryLevel.MID -> R.color.hh_success_green_600
                WirelessSwitchBatteryLevel.LOW -> R.color.hh_red
                WirelessSwitchBatteryLevel.UNKNOWN -> R.color.hh_grey
            })
            batteryView.setForegroundColor(color)
            batteryView.setBorderColor(color)
            batteryView.contentDescription = context.getString(when (level) {
                WirelessSwitchBatteryLevel.LOW -> R.string.text_battery_low
                WirelessSwitchBatteryLevel.MID -> R.string.text_battery_mid
                WirelessSwitchBatteryLevel.HIGH -> R.string.text_battery_high
                WirelessSwitchBatteryLevel.UNKNOWN -> R.string.text_unknown
            })
            batteryView.visibility = View.VISIBLE
            batteriesView.visibility = View.INVISIBLE
            messageView.visibility = View.INVISIBLE
        } else {
            batteriesView.setSwitches(info.switches)
            batteriesView.visibility = View.VISIBLE
            batteryView.visibility = View.INVISIBLE
            messageView.visibility = View.INVISIBLE
        }
        updateLayout()
        viewUpdated()
    }
}
