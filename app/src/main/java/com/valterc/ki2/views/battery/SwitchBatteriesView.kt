package com.valterc.ki2.views.battery

import android.content.Context
import android.content.res.Configuration
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import androidx.core.graphics.withTranslation
import com.valterc.ki2.R
import com.valterc.ki2.data.switches.SwitchSide
import com.valterc.ki2.data.switches.WirelessSwitchBatteryLevel
import com.valterc.ki2.data.switches.WirelessSwitchInfo
import com.valterc.ki2.data.switches.WirelessSwitchName
import kotlin.math.ceil
import kotlin.math.min
import kotlin.math.roundToInt

class SwitchBatteriesView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) : View(context, attrs) {
    private data class Row(val typeName: String?, val columns: List<List<BatteryView>>)

    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        val isDark = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES
        color = context.getColor(if (isDark) R.color.white else R.color.hh_black)
        textSize = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 13f, resources.displayMetrics)
    }
    private var rows = emptyList<Row>()

    val rowCount: Int
        get() = rows.size

    fun setSwitches(switches: List<WirelessSwitchInfo>) {
        val switchesByType = switches.sortedBy { it.slot }.groupBy { it.type }.toSortedMap()
        rows = switchesByType.values.map { rowSwitches ->
            val switchesBySide = rowSwitches.groupBy {
                when (it.side) {
                    SwitchSide.LEFT, SwitchSide.RIGHT -> it.side
                    else -> SwitchSide.NONE
                }
            }
            val hasSides = SwitchSide.LEFT in switchesBySide || SwitchSide.RIGHT in switchesBySide
            val columns = when {
                hasSides && SwitchSide.NONE in switchesBySide -> listOf(SwitchSide.LEFT, SwitchSide.NONE, SwitchSide.RIGHT)
                hasSides -> listOf(SwitchSide.LEFT, SwitchSide.RIGHT)
                else -> listOf(SwitchSide.NONE)
            }
            Row(
                if (switchesByType.size > 1) WirelessSwitchName.getShortTypeName(context, rowSwitches.first()).uppercase() else null,
                columns.map { side -> switchesBySide[side].orEmpty().map { createBatteryView(it.batteryLevel) } }
            )
        }
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (rows.isEmpty()) {
            return
        }

        val outerPadding = dp(5)
        val horizontalPadding = dp(4)
        val verticalPadding = 1
        val typeNameWidth = if (rows.size > 1) {
            ceil(rows.maxOf { textPaint.measureText(it.typeName.orEmpty()) }).toInt() + dp(6)
        } else {
            0
        }
        val contentLeft = outerPadding + typeNameWidth
        val contentWidth = (width - contentLeft - outerPadding).coerceAtLeast(0)
        val rowHeight = height.toFloat() / rows.size
        val cellWidth = min(
            contentWidth / 2f,
            rows.minOf { row -> contentWidth.toFloat() / row.columns.size / row.columns.maxOf { it.size.coerceAtLeast(1) } }
        )

        // All rows share a capped 1:2 size, even if a row contains only one battery.
        val batteryWidth = min(
            dp(MAX_BATTERY_WIDTH_DP).toFloat(),
            min(cellWidth - horizontalPadding * 2, (rowHeight - verticalPadding * 2) / 2)
        ).toInt().coerceAtLeast(0)
        val batteryHeight = batteryWidth * 2

        rows.forEachIndexed { rowIndex, row ->
            val centerY = rowHeight * (rowIndex + 0.5f)
            row.typeName?.let {
                canvas.drawText(it, outerPadding.toFloat(), centerY - (textPaint.ascent() + textPaint.descent()) / 2, textPaint)
            }

            if (batteryWidth > 0) {
                val columnWidth = contentWidth.toFloat() / row.columns.size
                row.columns.forEachIndexed { columnIndex, batteries ->
                    batteries.forEachIndexed { batteryIndex, battery ->
                        battery.borderStrokeWidth = batteryWidth * 0.1f
                        battery.measure(
                            MeasureSpec.makeMeasureSpec(batteryWidth, MeasureSpec.EXACTLY),
                            MeasureSpec.makeMeasureSpec(batteryHeight, MeasureSpec.EXACTLY)
                        )
                        battery.layout(0, 0, batteryWidth, batteryHeight)

                        val centerX = contentLeft + columnWidth * (columnIndex + (batteryIndex + 0.5f) / batteries.size)
                        canvas.withTranslation(
                            (centerX - batteryWidth / 2f).roundToInt().toFloat(),
                            (centerY - batteryHeight / 2f).roundToInt().toFloat()
                        ) {
                            clipRect(0, 0, batteryWidth, batteryHeight)
                            battery.draw(this)
                        }
                    }
                }
            }
        }
    }

    private fun createBatteryView(level: WirelessSwitchBatteryLevel): BatteryView {
        return BatteryView(context, null).apply {
            orientation = BatteryView.Orientation.VERTICAL_TOP
            value = when (level) {
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
            setForegroundColor(color)
            setBorderColor(color)
        }
    }

    private fun dp(value: Int): Int = (value * resources.displayMetrics.density).roundToInt()

    companion object {
        private const val MAX_BATTERY_WIDTH_DP = 32
    }
}
