package com.valterc.ki2.karoo.views

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import com.valterc.ki2.R
import com.valterc.ki2.data.connection.ConnectionInfo
import com.valterc.ki2.data.connection.ConnectionStatus
import com.valterc.ki2.data.device.BatteryInfo
import com.valterc.ki2.data.device.DeviceId
import com.valterc.ki2.data.preferences.PreferencesView
import com.valterc.ki2.karoo.Ki2ExtensionContext
import com.valterc.ki2.views.fill.FillView
import io.hammerhead.karooext.models.ViewConfig
import java.util.function.BiConsumer
import java.util.function.Consumer

class BikeBatteryExtensionView(context: Ki2ExtensionContext) : Ki2ExtensionView(context) {
    private val connectionInfoConsumer =
        BiConsumer { _: DeviceId?, connectionInfo: ConnectionInfo ->
            connectionStatus = connectionInfo.connectionStatus
            updateView()
        }

    private val batteryInfoConsumer =
        BiConsumer { _: DeviceId?, batteryInfo: BatteryInfo? ->
            this.batteryInfo = batteryInfo
            updateView()
        }

    private val preferencesConsumer =
        Consumer { preferencesView: PreferencesView ->
            batteryLevelLow = preferencesView.getBatteryLevelLow(context.context)
            batteryLevelCritical = preferencesView.getBatteryLevelCritical(context.context)
            updateView()
        }

    private var batteryLevelLow: Int? = null
    private var batteryLevelCritical: Int? = null
    private var connectionStatus: ConnectionStatus? = null
    private var batteryInfo: BatteryInfo? = null

    private var textView: TextView? = null
    private var fillView: FillView? = null

    init {
        context.serviceClient.registerPreferencesWeakListener(preferencesConsumer)
        context.serviceClient.registerConnectionInfoWeakListener(connectionInfoConsumer)
        context.serviceClient.registerBatteryInfoWeakListener(batteryInfoConsumer)
    }

    @SuppressLint("InflateParams")
    override fun createView(layoutInflater: LayoutInflater, viewConfig: ViewConfig): View {
        val inflatedView: View = layoutInflater.inflate(R.layout.view_karoo_battery, null)

        val params = ConstraintLayout.LayoutParams(
            ConstraintLayout.LayoutParams.MATCH_CONSTRAINT,
            ConstraintLayout.LayoutParams.MATCH_CONSTRAINT
        )
        params.matchConstraintMinWidth = viewConfig.viewSize.first
        params.matchConstraintMinHeight = viewConfig.viewSize.second

        inflatedView.layoutParams = params
        inflatedView.forceLayout()
        inflatedView.measure(
            View.MeasureSpec.makeMeasureSpec(viewConfig.viewSize.first, View.MeasureSpec.EXACTLY),
            View.MeasureSpec.makeMeasureSpec(viewConfig.viewSize.second, View.MeasureSpec.EXACTLY)
        )
        inflatedView.layout(
            inflatedView.left,
            inflatedView.top,
            inflatedView.right,
            inflatedView.bottom
        )

        textView = inflatedView.findViewById(R.id.textview_karoo_battery)
        fillView = inflatedView.findViewById(R.id.fillview_karoo_battery)

        if (karooTheme == KarooTheme.WHITE) {
            textView?.setTextColor(context.getColor(R.color.hh_black))
            textView?.setBackgroundColor(context.getColor(R.color.white))
        }

        updateView()
        return inflatedView
    }

    private fun updateView() {
        val fillView = fillView ?: return
        val textView = textView ?: return
        val batteryInfo = batteryInfo

        if (connectionStatus != ConnectionStatus.ESTABLISHED || batteryInfo == null) {
            fillView.setForegroundColor(context.getColor(R.color.hh_grey))
            fillView.value = 0f
            textView.setText(R.string.text_na)
        } else {
            val batteryLevelCritical = batteryLevelCritical
            val batteryLevelLow = batteryLevelLow

            if (batteryLevelCritical != null && batteryInfo.value <= batteryLevelCritical) {
                fillView.setForegroundColor(context.getColor(R.color.hh_red_600))
            } else if (batteryLevelLow != null && batteryInfo.value <= batteryLevelLow) {
                fillView.setForegroundColor(context.getColor(R.color.hh_yellow))
            } else {
                fillView.setForegroundColor(context.getColor(R.color.hh_success_green_600))
            }

            textView.text = context.getString(R.string.text_param_percentage, batteryInfo.value)
            fillView.value = batteryInfo.value * 0.01f
        }

        viewUpdated()
    }
}