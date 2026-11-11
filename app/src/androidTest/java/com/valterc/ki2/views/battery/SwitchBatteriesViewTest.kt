package com.valterc.ki2.views.battery

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Rect
import android.os.Parcel
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.RemoteViews
import android.widget.TextView
import androidx.core.graphics.createBitmap
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.valterc.ki2.R
import com.valterc.ki2.data.switches.SwitchSide
import com.valterc.ki2.data.switches.SwitchType
import com.valterc.ki2.data.switches.WirelessSwitchBatteryLevel
import com.valterc.ki2.data.switches.WirelessSwitchInfo
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.math.roundToInt

@RunWith(AndroidJUnit4::class)
class SwitchBatteriesViewTest {
    private val context = ApplicationProvider.getApplicationContext<Context>()
    private val maximumWidth = (32 * context.resources.displayMetrics.density).roundToInt()

    @Test
    fun batteriesStayVerticalAndEqualAcrossRowsAndViewportSizes() {
        val view = view(
            switch(1, SwitchType.ROAD_SHIFTER, SwitchSide.LEFT, WirelessSwitchBatteryLevel.LOW),
            switch(2, SwitchType.ROAD_SHIFTER, SwitchSide.RIGHT, WirelessSwitchBatteryLevel.HIGH),
            switch(3, SwitchType.TT_SWITCH, SwitchSide.NONE, WirelessSwitchBatteryLevel.UNKNOWN)
        )
        for ((width, height) in listOf(478 to 642, 478 to 126, 238 to 126, 238 to 642)) {
            val bitmap = render(view, width, height)
            val left = bounds(bitmap, R.color.hh_orange_dark)
            val right = bounds(bitmap, R.color.hh_green)
            val single = bounds(bitmap, R.color.hh_grey)

            assertEquals(left.width(), right.width())
            assertEquals(left.height(), right.height())
            assertEquals(left.width(), single.width())
            assertEquals(left.height(), single.height())
            assertEquals(2.0, left.height().toDouble() / left.width(), 0.1)
            assertTrue(left.width() <= maximumWidth)
            assertTrue(left.height() <= maximumWidth * 2)
            assertTrue(left.right < right.left)
            assertTrue(left.bottom < single.top)
            assertTrue(single.left > left.left)
            assertTrue(single.right < right.right)
            bitmap.recycle()
        }
    }

    @Test
    fun singleBatteryDoesNotGrowWhenSecondBatteryIsRemoved() {
        val single = switch(1, SwitchType.TT_SWITCH, SwitchSide.NONE, WirelessSwitchBatteryLevel.HIGH)
        val pairView = view(single, switch(2, SwitchType.TT_SWITCH, SwitchSide.NONE, WirelessSwitchBatteryLevel.LOW))
        val singleView = view(single)
        for ((width, height) in listOf(478 to 642, 478 to 126, 238 to 126)) {
            val pairBitmap = render(pairView, width, height)
            val singleBitmap = render(singleView, width, height)
            val paired = bounds(pairBitmap, R.color.hh_green)
            val alone = bounds(singleBitmap, R.color.hh_green)
            assertEquals(paired.width(), alone.width())
            assertEquals(paired.height(), alone.height())
            assertEquals(width / 2f, alone.exactCenterX(), 1f)
            pairBitmap.recycle()
            singleBitmap.recycle()
        }
    }

    @Test
    fun largeViewportUsesCappedNativeSizeRatherThanEnlargingBattery() {
        val view = view(switch(1, SwitchType.TT_SWITCH, SwitchSide.NONE, WirelessSwitchBatteryLevel.HIGH))
        val bitmap = render(view, 478, 642)
        val actual = bounds(bitmap, R.color.hh_green)
        assertEquals(maximumWidth.toDouble(), actual.width().toDouble(), 1.0)
        assertTrue(actual.height() <= maximumWidth * 2)

        val nativeBattery = BatteryView(context, null).apply {
            orientation = BatteryView.Orientation.VERTICAL_TOP
            borderStrokeWidth = maximumWidth * 0.1f
            value = 1f
            setForegroundColor(context.getColor(R.color.hh_green))
            setBorderColor(context.getColor(R.color.hh_green))
        }
        val expected = render(nativeBattery, maximumWidth, maximumWidth * 2)
        val expectedBounds = bounds(expected, R.color.hh_green)
        assertEquals(expectedBounds.width(), actual.width())
        assertEquals(expectedBounds.height(), actual.height())
        val cropped = Bitmap.createBitmap(
            bitmap,
            ((478 - maximumWidth) / 2f).roundToInt(),
            321 - maximumWidth,
            maximumWidth,
            maximumWidth * 2
        )
        assertTrue("Battery pixels must match the native-size rendering", expected.sameAs(cropped))
        expected.recycle()
        cropped.recycle()
        bitmap.recycle()
    }

    @Test
    fun remoteHostReceivesExactViewportWithoutRescaling() {
        val view = layoutWithBattery()
        for ((width, height) in listOf(478 to 642, 478 to 126, 238 to 126)) {
            val bitmap = render(view, width, height)
            val remoteViews = RemoteViews(context.packageName, R.layout.remote_view_image).apply {
                setImageViewBitmap(R.id.imageView_remote, bitmap)
            }
            val parcel = Parcel.obtain()
            val received = try {
                remoteViews.writeToParcel(parcel, 0)
                parcel.setDataPosition(0)
                RemoteViews.CREATOR.createFromParcel(parcel)
            } finally {
                parcel.recycle()
            }
            val hostContext = InstrumentationRegistry.getInstrumentation().context
            val hostView = received.apply(hostContext, FrameLayout(hostContext))
            val hostBitmap = render(hostView, width, height)
            val image = hostView.findViewById<ImageView>(R.id.imageView_remote)
            assertEquals(width, image.width)
            assertEquals(height, image.height)
            assertTrue("Remote host must not scale the viewport bitmap", image.imageMatrix.isIdentity)
            assertTrue(bitmap.sameAs(hostBitmap))
            bitmap.recycle()
            hostBitmap.recycle()
        }
    }

    @Test
    fun headerShowsTitleAndIconWithoutOverlappingBatteriesOrMessages() {
        for ((width, height) in listOf(478 to 642, 478 to 126, 238 to 126)) {
            val view = layoutWithBattery()
            val bitmap = render(view, width, height)
            val header = view.findViewById<View>(R.id.linearlayout_karoo_switch_batteries_header)
            val title = view.findViewById<TextView>(R.id.textview_karoo_switch_batteries_header)
            val icon = view.findViewById<ImageView>(R.id.imageview_karoo_switch_batteries_header)
            val content = view.findViewById<View>(R.id.framelayout_karoo_switch_batteries_content)
            val batteries = view.findViewById<SwitchBatteriesView>(R.id.switchbatteriesview_karoo)
            val message = view.findViewById<TextView>(R.id.textview_karoo_switch_batteries_message)
            val battery = bounds(bitmap, R.color.hh_green)

            assertEquals(context.getString(R.string.text_switch_batteries), title.text.toString())
            assertTrue(title.width > 0 && title.height > 0)
            assertEquals(if (width == 238) 2 else 1, title.layout.lineCount)
            for (line in 0 until title.layout.lineCount) {
                assertEquals("The full header title must fit without truncation", 0, title.layout.getEllipsisCount(line))
                assertTrue(title.layout.getLineWidth(line) <= title.width)
            }
            assertTrue(title.layout.height <= title.height)
            assertTrue(icon.drawable != null && icon.width > 0 && icon.height > 0)
            assertTrue(header.bottom <= content.top)
            assertTrue(content.height > 0)
            assertTrue(battery.top >= content.top && battery.bottom <= content.bottom)
            assertEquals(2.0, battery.height().toDouble() / battery.width(), 0.1)
            assertTrue(battery.width() <= maximumWidth)
            assertTrue(battery.height() <= maximumWidth * 2)

            batteries.visibility = View.INVISIBLE
            message.visibility = View.VISIBLE
            val waitingBitmap = render(view, width, height)
            assertEquals(View.VISIBLE, header.visibility)
            assertEquals(content.height, message.height)
            assertEquals(context.getString(R.string.text_waiting_for_data), message.text.toString())
            waitingBitmap.recycle()
            bitmap.recycle()
        }
    }

    @Test
    fun singleSquareKeepsSingleAndPairedBatteriesReadable() {
        val minimumHeight = (24 * context.resources.displayMetrics.density).roundToInt()
        for (paired in listOf(false, true)) {
            val view = layoutWithBattery()
            if (paired) {
                view.findViewById<SwitchBatteriesView>(R.id.switchbatteriesview_karoo).setSwitches(
                    listOf(
                        switch(1, SwitchType.ROAD_SHIFTER, SwitchSide.LEFT, WirelessSwitchBatteryLevel.HIGH),
                        switch(2, SwitchType.ROAD_SHIFTER, SwitchSide.RIGHT, WirelessSwitchBatteryLevel.LOW)
                    )
                )
            }
            val bitmap = render(view, 238, 126)
            val battery = bounds(bitmap, R.color.hh_green)
            assertTrue("Single-square battery height ${battery.height()} must be at least $minimumHeight pixels", battery.height() >= minimumHeight)
            assertEquals(2.0, battery.height().toDouble() / battery.width(), 0.1)
            if (paired) {
                val other = bounds(bitmap, R.color.hh_orange_dark)
                assertEquals(battery.width(), other.width())
                assertEquals(battery.height(), other.height())
                assertTrue(battery.right < other.left)
            }
            bitmap.recycle()
        }
    }

    @Test
    fun mixedSidesAndRepeatedSideDoNotOverlap() {
        val bitmap = render(
            view(
                switch(1, SwitchType.ROAD_SHIFTER, SwitchSide.LEFT, WirelessSwitchBatteryLevel.LOW),
                switch(2, SwitchType.ROAD_SHIFTER, SwitchSide.LEFT, WirelessSwitchBatteryLevel.HIGH),
                switch(3, SwitchType.ROAD_SHIFTER, SwitchSide.NONE, WirelessSwitchBatteryLevel.UNKNOWN)
            ), 238, 126
        )
        val first = bounds(bitmap, R.color.hh_orange_dark)
        val second = bounds(bitmap, R.color.hh_green)
        val center = bounds(bitmap, R.color.hh_grey)
        assertEquals(first.width(), second.width())
        assertEquals(first.height(), second.height())
        assertEquals(first.width(), center.width())
        assertEquals(first.height(), center.height())
        assertTrue(first.right < second.left)
        assertTrue(second.right < center.left)
        bitmap.recycle()
    }

    private fun switch(slot: Int, type: SwitchType, side: SwitchSide, level: WirelessSwitchBatteryLevel) =
        WirelessSwitchInfo(slot, type, side, level)

    private fun view(vararg switches: WirelessSwitchInfo) = SwitchBatteriesView(context).apply {
        setSwitches(switches.toList())
    }

    private fun layoutWithBattery(): View {
        return LayoutInflater.from(context).inflate(R.layout.view_karoo_switch_batteries, null).apply {
            findViewById<SwitchBatteriesView>(R.id.switchbatteriesview_karoo).apply {
                setSwitches(listOf(switch(1, SwitchType.TT_SWITCH, SwitchSide.NONE, WirelessSwitchBatteryLevel.HIGH)))
                visibility = View.VISIBLE
            }
            findViewById<TextView>(R.id.textview_karoo_switch_batteries_message).visibility = View.INVISIBLE
        }
    }

    private fun render(view: View, width: Int, height: Int): Bitmap {
        view.measure(
            View.MeasureSpec.makeMeasureSpec(width, View.MeasureSpec.EXACTLY),
            View.MeasureSpec.makeMeasureSpec(height, View.MeasureSpec.EXACTLY)
        )
        view.layout(0, 0, width, height)
        return createBitmap(width, height).also { view.draw(Canvas(it)) }
    }

    private fun bounds(bitmap: Bitmap, colorId: Int): Rect {
        val color = context.getColor(colorId)
        val bounds = Rect()
        for (y in 0 until bitmap.height) {
            for (x in 0 until bitmap.width) {
                if (bitmap.getPixel(x, y) == color) {
                    bounds.union(x, y, x + 1, y + 1)
                }
            }
        }
        assertFalse("Battery should be visible", bounds.isEmpty)
        return bounds
    }
}
