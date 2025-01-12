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
import com.valterc.ki2.data.preferences.device.DevicePreferencesView
import com.valterc.ki2.data.shifting.ShiftingInfo
import com.valterc.ki2.karoo.Ki2ExtensionContext
import com.valterc.ki2.karoo.shifting.ShiftingGearingHelper
import com.valterc.ki2.views.GearsView
import com.valterc.ki2.views.battery.BatteryView
import io.hammerhead.karooext.models.ViewConfig
import java.util.function.BiConsumer
import java.util.function.Consumer


class GearsExtensionView(context: Ki2ExtensionContext, private val showGearSize: Boolean = false) : Ki2ExtensionView(context) {
    private val devicePreferencesConsumer =
        BiConsumer<DeviceId, DevicePreferencesView> { _: DeviceId?, devicePreferences: DevicePreferencesView? ->
            shiftingGearingHelper.setDevicePreferences(devicePreferences)
            updateView()
        }

    private val connectionInfoConsumer =
        BiConsumer { _: DeviceId?, connectionInfo: ConnectionInfo ->
            connectionStatus = connectionInfo.connectionStatus
            updateView()
        }

    private val shiftingInfoConsumer =
        BiConsumer { _: DeviceId?, shiftingInfo: ShiftingInfo? ->
            shiftingGearingHelper.setShiftingInfo(shiftingInfo)
            updateView()
        }

    private val batteryInfoConsumer =
        BiConsumer { _: DeviceId?, batteryInfo: BatteryInfo? ->
            this.batteryInfo = batteryInfo
            updateView()
        }

    private val preferencesConsumer =
        Consumer { preferencesView: PreferencesView? ->
            this.preferencesView = preferencesView
            updateView()
        }

    private val shiftingGearingHelper = ShiftingGearingHelper(context.context)
    private var connectionStatus: ConnectionStatus? = null
    private var batteryInfo: BatteryInfo? = null
    private var preferencesView: PreferencesView? = null

    private var textViewWaitingForData: TextView? = null
    private var gearsView: GearsView? = null
    private var batteryView: BatteryView? = null
    private var textViewGears: TextView? = null

    init {
        serviceClient.registerDevicePreferencesWeakListener(devicePreferencesConsumer)
        serviceClient.registerConnectionInfoWeakListener(connectionInfoConsumer)
        serviceClient.registerShiftingInfoWeakListener(shiftingInfoConsumer)
        serviceClient.registerBatteryInfoWeakListener(batteryInfoConsumer)
        serviceClient.registerPreferencesWeakListener(preferencesConsumer)
    }

    override fun dispose() {
        super.dispose()
        serviceClient.unregisterDevicePreferencesWeakListener(devicePreferencesConsumer)
        serviceClient.unregisterConnectionInfoWeakListener(connectionInfoConsumer)
        serviceClient.unregisterShiftingInfoWeakListener(shiftingInfoConsumer)
        serviceClient.unregisterBatteryInfoWeakListener(batteryInfoConsumer)
        serviceClient.unregisterPreferencesWeakListener(preferencesConsumer)
    }

    @SuppressLint("InflateParams")
    override fun createView(layoutInflater: LayoutInflater, viewConfig: ViewConfig): View {
        val inflatedView = layoutInflater.inflate(R.layout.view_karoo_gears, null)

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

        textViewWaitingForData =
            inflatedView.findViewById(R.id.textview_karoo_gears_waiting_for_data)
        batteryView = inflatedView.findViewById(R.id.batteryview_karoo_gears)
        textViewGears = inflatedView.findViewById(R.id.textview_karoo_gears)
        gearsView = inflatedView.findViewById(R.id.gearsview_karoo_gears)

        if (karooTheme == KarooTheme.WHITE) {
            textViewWaitingForData?.setTextColor(context.getColor(R.color.hh_black))
            textViewGears?.setTextColor(context.getColor(R.color.hh_black))
            gearsView?.setUnselectedGearBorderColor(context.getColor(R.color.hh_gears_border_light))
            gearsView?.setSelectedGearColor(context.getColor(R.color.hh_gears_active_light))
        } else {
            textViewWaitingForData?.setTextColor(context.getColor(R.color.white))
            textViewGears?.setTextColor(context.getColor(R.color.white))
            gearsView?.setUnselectedGearBorderColor(context.getColor(R.color.hh_gears_border_dark))
            gearsView?.setSelectedGearColor(context.getColor(R.color.hh_gears_active_dark))
        }

        updateView()
        return inflatedView
    }

    private fun updateView() {
        val gearsView = gearsView ?: return
        val batteryView = batteryView ?: return
        val textViewGears = textViewGears ?: return
        val textViewWaitingForData = textViewWaitingForData ?: return

        if (connectionStatus != ConnectionStatus.ESTABLISHED || shiftingGearingHelper.hasInvalidGearingInfo() || preferencesView == null) {
            gearsView.visibility = View.INVISIBLE
            batteryView.visibility = View.INVISIBLE
            textViewGears.visibility = View.INVISIBLE
            textViewWaitingForData.visibility = View.VISIBLE

            viewUpdated()
            return
        } else {
            textViewWaitingForData.visibility = View.INVISIBLE
            gearsView.visibility = View.VISIBLE
            batteryView.visibility = View.VISIBLE
            textViewGears.visibility = View.VISIBLE
        }

        val preferencesView = preferencesView ?: return

        gearsView.setGears(
            shiftingGearingHelper.frontGearMax,
            shiftingGearingHelper.frontGear,
            shiftingGearingHelper.rearGearMax,
            shiftingGearingHelper.rearGear
        )

        if (showGearSize) {
            textViewGears.text = context.getString(
                R.string.text_param_gearing,
                shiftingGearingHelper.frontGearTeethCount,
                shiftingGearingHelper.rearGearTeethCount
            )
        } else {
            textViewGears.text = context.getString(
                R.string.text_param_gearing,
                shiftingGearingHelper.frontGear,
                shiftingGearingHelper.rearGear
            )
        }

        gearsView.selectedGearColor = preferencesView.getAccentColor(context, karooTheme)

        batteryInfo?.let { batteryInfo ->
            batteryView.value = batteryInfo.value.toFloat() / 100

            val criticalBatteryLevel = preferencesView.getBatteryLevelCritical(context)
            val lowBatteryLevel = preferencesView.getBatteryLevelLow(context)

            if (criticalBatteryLevel != null && batteryInfo.value <= criticalBatteryLevel) {
                batteryView.setForegroundColor(context.getColor(R.color.hh_red))
                batteryView.setBorderColor(context.getColor(R.color.hh_red))
            } else if (lowBatteryLevel != null && batteryInfo.value <= lowBatteryLevel) {
                batteryView.setForegroundColor(context.getColor(R.color.hh_yellow_darker))
                batteryView.setBorderColor(context.getColor(R.color.hh_yellow_darker))
            } else {
                batteryView.setForegroundColor(context.getColor(R.color.hh_green))
                batteryView.setBorderColor(context.getColor(R.color.hh_green))
            }
        } ?: run {
            batteryView.setForegroundColor(context.getColor(R.color.battery_background_dark))
            batteryView.setBorderColor(context.getColor(R.color.battery_border_dark))
        }

        viewUpdated()
    }
}