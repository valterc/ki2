package com.valterc.ki2.karoo.views

import android.annotation.SuppressLint
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import com.valterc.ki2.R
import com.valterc.ki2.data.connection.ConnectionInfo
import com.valterc.ki2.data.device.DeviceId
import com.valterc.ki2.data.switches.WirelessSwitchesInfo
import com.valterc.ki2.karoo.Ki2ExtensionContext
import com.valterc.ki2.views.battery.SwitchBatteriesView
import io.hammerhead.karooext.models.ViewConfig
import java.util.function.BiConsumer

class SwitchBatteriesExtensionView(context: Ki2ExtensionContext) : Ki2ExtensionView(context) {
    private var connectionDeviceId: DeviceId? = null
    private var connectionInfo: ConnectionInfo? = null
    private var switchesDeviceId: DeviceId? = null
    private var switchesInfo: WirelessSwitchesInfo? = null
    private var batteriesView: SwitchBatteriesView? = null
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
        batteriesView = view.findViewById(R.id.switchbatteriesview_karoo)
        val textColor = context.getColor(if (karooTheme == KarooTheme.WHITE) R.color.hh_black else R.color.white)
        view.findViewById<TextView>(R.id.textview_karoo_switch_batteries_header).setTextColor(textColor)
        messageView = view.findViewById<TextView>(R.id.textview_karoo_switch_batteries_message).apply {
            setTextColor(textColor)
            textSize = if (viewConfig.viewSize.first < context.resources.displayMetrics.widthPixels * 0.75) 14f else 18f
            gravity = Gravity.CENTER_VERTICAL or when (viewConfig.alignment) {
                ViewConfig.Alignment.LEFT -> Gravity.START
                else -> Gravity.END
            }
        }
        updateView()

        view.measure(
            View.MeasureSpec.makeMeasureSpec(viewConfig.viewSize.first, View.MeasureSpec.EXACTLY),
            View.MeasureSpec.makeMeasureSpec(viewConfig.viewSize.second, View.MeasureSpec.EXACTLY)
        )
        view.layout(0, 0, viewConfig.viewSize.first, viewConfig.viewSize.second)
        return view
    }

    private fun updateView() {
        val batteriesView = batteriesView ?: return
        val messageView = messageView ?: return
        val info = switchesInfo?.takeIf { switchesDeviceId == connectionDeviceId }

        if (connectionInfo?.isConnected != true || info == null) {
            batteriesView.visibility = View.INVISIBLE
            messageView.visibility = View.VISIBLE
            messageView.setText(R.string.text_waiting_for_data)
        } else if (info.switches.isEmpty()) {
            batteriesView.visibility = View.INVISIBLE
            messageView.visibility = View.VISIBLE
            messageView.setText(R.string.text_no_switches)
        } else {
            batteriesView.setSwitches(info.switches)
            batteriesView.visibility = View.VISIBLE
            messageView.visibility = View.INVISIBLE
        }
        viewUpdated()
    }
}
