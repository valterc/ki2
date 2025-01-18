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
import com.valterc.ki2.views.DrivetrainView
import com.valterc.ki2.views.battery.BatteryView
import io.hammerhead.karooext.models.ViewConfig
import java.util.function.BiConsumer
import java.util.function.Consumer

class DrivetrainExtensionView(context: Ki2ExtensionContext, private val showGearSize: Boolean = false) : Ki2ExtensionView(context) {
    private val devicePreferencesConsumer =
        BiConsumer<DeviceId, DevicePreferencesView> { _: DeviceId?, devicePreferences: DevicePreferencesView? ->
            shiftingGearingHelper.setDevicePreferences(devicePreferences)
            updateView()
        }

    private val connectionInfoConsumer: BiConsumer<DeviceId, ConnectionInfo> =
        BiConsumer<DeviceId, ConnectionInfo> { _: DeviceId?, connectionInfo: ConnectionInfo ->
            connectionStatus = connectionInfo.connectionStatus
            updateView()
        }

    private val shiftingInfoConsumer: BiConsumer<DeviceId, ShiftingInfo> =
        BiConsumer<DeviceId, ShiftingInfo> { _: DeviceId?, shiftingInfo: ShiftingInfo? ->
            shiftingGearingHelper.setShiftingInfo(shiftingInfo)
            updateView()
        }

    private val batteryInfoConsumer: BiConsumer<DeviceId, BatteryInfo> =
        BiConsumer<DeviceId, BatteryInfo> { _: DeviceId?, batteryInfo: BatteryInfo? ->
            this.batteryInfo = batteryInfo
            updateView()
        }

    private val preferencesConsumer: Consumer<PreferencesView> =
        Consumer<PreferencesView> { preferencesView ->
            this.preferencesView = preferencesView
            updateView()
        }

    private val shiftingGearingHelper = ShiftingGearingHelper(context.context)
    private var connectionStatus: ConnectionStatus? = null
    private var batteryInfo: BatteryInfo? = null
    private var preferencesView: PreferencesView? = null

    private var textViewWaitingForData: TextView? = null
    private var drivetrainView: DrivetrainView? = null
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
        val inflatedView: View = layoutInflater.inflate(R.layout.view_karoo_drivetrain, null)

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
            inflatedView.findViewById(R.id.textview_karoo_drivetrain_waiting_for_data)
        drivetrainView =
            inflatedView.findViewById(R.id.drivetrainview_karoo_drivetrain)
        batteryView = inflatedView.findViewById(R.id.batteryview_karoo_drivetrain)
        textViewGears = inflatedView.findViewById(R.id.textview_karoo_drivetrain)

        if (karooTheme == KarooTheme.WHITE) {
            textViewWaitingForData?.setTextColor(context.getColor(R.color.hh_black))
            textViewGears?.setTextColor(context.getColor(R.color.hh_black))
            drivetrainView?.setTextColor(context.getColor(R.color.hh_black))
            drivetrainView?.setChainColor(context.getColor(R.color.hh_black))
        } else {
            textViewWaitingForData?.setTextColor(context.getColor(R.color.white))
            textViewGears?.setTextColor(context.getColor(R.color.white))
            drivetrainView?.setTextColor(context.getColor(R.color.white))
            drivetrainView?.setChainColor(context.getColor(R.color.hh_divider_color))
        }

        updateView()
        return inflatedView
    }

    private fun updateView() {
        val drivetrainView = drivetrainView ?: return
        val batteryView = batteryView ?: return
        val textViewGears = textViewGears ?: return
        val textViewWaitingForData = textViewWaitingForData ?: return

        if (connectionStatus != ConnectionStatus.ESTABLISHED || shiftingGearingHelper.hasInvalidGearingInfo() || preferencesView == null) {
            drivetrainView.visibility = View.INVISIBLE
            batteryView.visibility = View.INVISIBLE
            textViewGears.visibility = View.INVISIBLE
            textViewWaitingForData.visibility = View.VISIBLE

            viewUpdated()
            return
        } else {
            textViewWaitingForData.visibility = View.INVISIBLE
            batteryView.visibility = View.VISIBLE
            textViewGears.visibility = View.VISIBLE
            drivetrainView.visibility = View.VISIBLE
        }

        val preferencesView = preferencesView ?: return

        drivetrainView.setGears(
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

        drivetrainView.selectedGearColor = preferencesView.getAccentColor(context, karooTheme)

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
                batteryView.setForegroundColor(context.getColor(R.color.hh_success_green_600))
                batteryView.setBorderColor(context.getColor(R.color.hh_success_green_600))
            }
        } ?: run {
            batteryView.setForegroundColor(context.getColor(R.color.battery_background_dark))
            batteryView.setBorderColor(context.getColor(R.color.battery_border_dark))
        }

        viewUpdated()
    }
}