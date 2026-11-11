package com.valterc.ki2.views.battery

import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.ServiceConnection
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Rect
import android.os.Parcel
import android.text.Layout
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.RemoteViews
import android.widget.TextView
import androidx.core.graphics.createBitmap
import androidx.compose.ui.unit.DpSize
import androidx.glance.appwidget.ExperimentalGlanceRemoteViewsApi
import androidx.glance.appwidget.GlanceRemoteViews
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.valterc.ki2.R
import com.valterc.ki2.data.connection.ConnectionInfo
import com.valterc.ki2.data.connection.ConnectionStatus
import com.valterc.ki2.data.device.DeviceId
import com.valterc.ki2.data.switches.SwitchSide
import com.valterc.ki2.data.switches.SwitchType
import com.valterc.ki2.data.switches.WirelessSwitchBatteryLevel
import com.valterc.ki2.data.switches.WirelessSwitchInfo
import com.valterc.ki2.data.switches.WirelessSwitchesInfo
import com.valterc.ki2.karoo.Ki2ExtensionContext
import com.valterc.ki2.karoo.datatypes.RenderedVisualDataType
import com.valterc.ki2.karoo.datatypes.views.NotAvailable
import com.valterc.ki2.karoo.datatypes.views.Waiting
import com.valterc.ki2.karoo.datatypes.visual.SwitchBatteryVisualDataType
import com.valterc.ki2.karoo.views.SwitchBatteriesExtensionView
import io.hammerhead.karooext.models.ViewConfig
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import java.util.function.BiConsumer
import kotlin.math.roundToInt
import kotlin.math.ceil
import com.valterc.ki2.karoo.datatypes.views.TextView as DataTextView

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
            val view = layoutWithBattery(width, height)
            val bitmap = render(view, width, height)
            val header = view.findViewById<View>(R.id.linearlayout_karoo_switch_batteries_header)
            val title = view.findViewById<TextView>(R.id.textview_karoo_switch_batteries_header)
            val icon = view.findViewById<ImageView>(R.id.imageview_karoo_switch_batteries_header)
            val content = view.findViewById<View>(R.id.framelayout_karoo_switch_batteries_content)
            val batteries = view.findViewById<SwitchBatteriesView>(R.id.switchbatteriesview_karoo)
            val message = view.findViewById<TextView>(R.id.textview_karoo_switch_batteries_message)
            val battery = bounds(bitmap, R.color.hh_success_green_600)

            assertEquals(context.getString(R.string.text_switch_batteries), title.text.toString())
            assertTrue(title.width > 0 && title.height > 0)
            assertTitleFits(title)
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
    fun switchBatteriesPreserveLevelFillsAndColors() {
        val switchesView = view()
        val cases = listOf(
            Triple(WirelessSwitchBatteryLevel.HIGH, 1f, R.color.hh_success_green_600),
            Triple(WirelessSwitchBatteryLevel.MID, 0.5f, R.color.hh_success_green_600),
            Triple(WirelessSwitchBatteryLevel.LOW, 0.15f, R.color.hh_red),
            Triple(WirelessSwitchBatteryLevel.UNKNOWN, 0f, R.color.hh_grey)
        )
        for ((level, expectedValue, colorId) in cases + cases.reversed()) {
            switchesView.setSwitches(listOf(switch(1, SwitchType.TT_SWITCH, SwitchSide.NONE, level)))
            val bitmap = render(switchesView, maximumWidth * 4, maximumWidth * 2 + 2)
            val batteryView = BatteryView(context, null).apply {
                orientation = BatteryView.Orientation.VERTICAL_TOP
                borderStrokeWidth = maximumWidth * 0.1f
                value = expectedValue
                setForegroundColor(context.getColor(colorId))
                setBorderColor(context.getColor(colorId))
            }
            val expected = render(batteryView, maximumWidth, maximumWidth * 2)
            val actual = Bitmap.createBitmap(
                bitmap,
                ((bitmap.width - expected.width) / 2f).roundToInt(),
                ((bitmap.height - expected.height) / 2f).roundToInt(),
                expected.width,
                expected.height
            )
            assertTrue("Battery fill and color must match $level", expected.sameAs(actual))
            actual.recycle()
            expected.recycle()
            bitmap.recycle()
        }
    }

    @Test
    fun lowestBatteryVisualAlwaysUsesFullTitleAndBatteryView() {
        assertTrue(RenderedVisualDataType::class.java.isAssignableFrom(SwitchBatteryVisualDataType::class.java))
        val cases = listOf(
            listOf(WirelessSwitchBatteryLevel.HIGH) to WirelessSwitchBatteryLevel.HIGH,
            listOf(WirelessSwitchBatteryLevel.HIGH, WirelessSwitchBatteryLevel.MID) to WirelessSwitchBatteryLevel.MID,
            listOf(WirelessSwitchBatteryLevel.HIGH, WirelessSwitchBatteryLevel.UNKNOWN, WirelessSwitchBatteryLevel.LOW) to WirelessSwitchBatteryLevel.LOW,
            listOf(WirelessSwitchBatteryLevel.UNKNOWN) to WirelessSwitchBatteryLevel.UNKNOWN
        )
        for ((width, height) in listOf(478 to 642, 478 to 126, 238 to 126)) {
            for ((levels, expected) in cases) {
                val view = layoutWithBattery(width, height, showLowestBatteryOnly = true, levels = levels)
                val bitmap = render(view, width, height)
                val title = view.findViewById<TextView>(R.id.textview_karoo_switch_batteries_header)
                val batteryView = view.findViewById<BatteryView>(R.id.batteryview_karoo_switch_battery)
                assertEquals(context.getString(R.string.text_switch_battery), title.text.toString())
                assertFalse(title.text.contains("(visual)"))
                assertTitleFits(title)
                assertEquals(View.VISIBLE, batteryView.visibility)
                assertEquals(BatteryView.Orientation.VERTICAL_TOP, batteryView.orientation)
                assertEquals(batteryView.width * 2, batteryView.height)
                assertEquals(View.INVISIBLE, view.findViewById<View>(R.id.switchbatteriesview_karoo).visibility)
                assertEquals(View.INVISIBLE, view.findViewById<View>(R.id.textview_karoo_switch_batteries_message).visibility)
                val colorId = when (expected) {
                    WirelessSwitchBatteryLevel.HIGH, WirelessSwitchBatteryLevel.MID -> R.color.hh_success_green_600
                    WirelessSwitchBatteryLevel.LOW -> R.color.hh_red
                    WirelessSwitchBatteryLevel.UNKNOWN -> R.color.hh_grey
                }
                assertEquals(context.getColor(colorId), batteryView.foregroundColor)
                assertEquals(context.getColor(colorId), batteryView.borderColor)
                val expectedValue = when (expected) {
                    WirelessSwitchBatteryLevel.HIGH -> 1f
                    WirelessSwitchBatteryLevel.MID -> 0.5f
                    WirelessSwitchBatteryLevel.LOW -> 0.15f
                    WirelessSwitchBatteryLevel.UNKNOWN -> 0f
                }
                assertEquals(expectedValue, batteryView.value, 0f)
                assertEquals(if (expected == WirelessSwitchBatteryLevel.UNKNOWN) "Unknown" else expected.name, batteryView.contentDescription)
                val battery = bounds(bitmap, colorId)
                assertTrue(battery.top >= view.findViewById<View>(R.id.linearlayout_karoo_switch_batteries_header).bottom)
                bitmap.recycle()
            }
        }
    }

    @Test
    fun headerAbbreviatesOnlyForMultipleRowsAndInsufficientWidth() {
        val oneRow = listOf(
            switch(1, SwitchType.TT_SWITCH, SwitchSide.NONE, WirelessSwitchBatteryLevel.HIGH),
            switch(2, SwitchType.TT_SWITCH, SwitchSide.NONE, WirelessSwitchBatteryLevel.MID),
            switch(3, SwitchType.TT_SWITCH, SwitchSide.NONE, WirelessSwitchBatteryLevel.LOW)
        )
        val twoRows = listOf(
            oneRow.first(),
            switch(2, SwitchType.ROAD_SHIFTER, SwitchSide.LEFT, WirelessSwitchBatteryLevel.LOW)
        )
        val threeRows = twoRows + switch(3, SwitchType.MTB_SWITCH, SwitchSide.RIGHT, WirelessSwitchBatteryLevel.MID)
        for (showLowestBatteryOnly in listOf(false, true)) {
            val fullTitle = context.getString(if (showLowestBatteryOnly) R.string.text_switch_battery else R.string.text_switch_batteries)
            val shortTitle = context.getString(R.string.text_short_switch_batteries)
            val reference = layoutWithBattery(showLowestBatteryOnly = showLowestBatteryOnly)
            val header = reference.findViewById<TextView>(R.id.textview_karoo_switch_batteries_header)
            val fullText = header.transformationMethod.getTransformation(fullTitle, header)
            val shortText = header.transformationMethod.getTransformation(shortTitle, header)
            val widthOutsideText = reference.width - header.width + header.compoundPaddingLeft + header.compoundPaddingRight
            val fullWidth = ceil(Layout.getDesiredWidth(fullText, header.paint)).toInt() + widthOutsideText
            val shortWidth = ceil(Layout.getDesiredWidth(shortText, header.paint)).toInt() + widthOutsideText
            for (gridSize in listOf(1 to 1, 2 to 1, 30 to 30, 60 to 30)) {
                for (width in listOf(fullWidth + 1, fullWidth - 1, shortWidth)) {
                    for (switches in listOf(emptyList(), oneRow.take(1), oneRow, twoRows, threeRows)) {
                        val view = layoutWithBattery(
                            width, showLowestBatteryOnly = showLowestBatteryOnly,
                            gridSize = gridSize, switches = switches
                        )
                        val bitmap = render(view, width, reference.height)
                        val title = view.findViewById<TextView>(R.id.textview_karoo_switch_batteries_header)
                        val rowCount = switches.map { it.type }.distinct().size
                        val shouldShorten = !showLowestBatteryOnly && rowCount > 1 && width < fullWidth
                        assertEquals(if (shouldShorten) shortTitle else fullTitle, title.text.toString())
                        if (shouldShorten) {
                            assertEquals(1, title.layout.lineCount)
                        }
                        assertTitleFits(title)
                        bitmap.recycle()
                    }
                }
            }
        }
    }

    @Test
    fun headerUpdatesWhenVisibleBatteryRowsChange() {
        val deviceId = DeviceId(1, 1, 1)
        val oneRow = listOf(switch(1, SwitchType.TT_SWITCH, SwitchSide.NONE, WirelessSwitchBatteryLevel.HIGH))
        val twoRows = oneRow + switch(2, SwitchType.ROAD_SHIFTER, SwitchSide.LEFT, WirelessSwitchBatteryLevel.LOW)
        for (showLowestBatteryOnly in listOf(false, true)) {
            layoutWithBattery(238, showLowestBatteryOnly = showLowestBatteryOnly, switches = twoRows) { extensionView, view ->
                val title = view.findViewById<TextView>(R.id.textview_karoo_switch_batteries_header)
                val fullTitle = context.getString(if (showLowestBatteryOnly) R.string.text_switch_battery else R.string.text_switch_batteries)
                val multipleRowsTitle = if (showLowestBatteryOnly) fullTitle else context.getString(R.string.text_short_switch_batteries)
                assertEquals(multipleRowsTitle, title.text.toString())

                val switchesConsumer = consumer<WirelessSwitchesInfo>(extensionView, "switchesConsumer")
                switchesConsumer.accept(deviceId, WirelessSwitchesInfo(oneRow))
                assertEquals(fullTitle, title.text.toString())
                assertTitleFits(title)

                switchesConsumer.accept(deviceId, WirelessSwitchesInfo(twoRows))
                assertEquals(multipleRowsTitle, title.text.toString())
                assertTitleFits(title)

                consumer<ConnectionInfo>(extensionView, "connectionInfoConsumer").accept(
                    deviceId, ConnectionInfo(ConnectionStatus.CLOSED)
                )
                assertEquals(fullTitle, title.text.toString())
                assertTitleFits(title)
            }
        }
    }

    @OptIn(ExperimentalGlanceRemoteViewsApi::class)
    @Test
    fun batteryTextAndStatusViewsHaveTransparentBackgroundsInBothThemes() = runBlocking {
        for (nightMode in listOf(Configuration.UI_MODE_NIGHT_NO, Configuration.UI_MODE_NIGHT_YES)) {
            val configuration = Configuration(context.resources.configuration).apply {
                uiMode = (uiMode and Configuration.UI_MODE_NIGHT_MASK.inv()) or nightMode
            }
            val themedContext = context.createConfigurationContext(configuration)
            for (text in listOf("LOW", "...", "N/A")) {
                val remoteViews = GlanceRemoteViews().compose(themedContext, DpSize.Unspecified) {
                    when (text) {
                        "..." -> Waiting(transparentBackground = true)
                        "N/A" -> NotAvailable(transparentBackground = true)
                        else -> DataTextView(text, fontSize = 30, transparentBackground = true)
                    }
                }.remoteViews
                val view = remoteViews.apply(themedContext, FrameLayout(themedContext))
                val bitmap = render(view, 238, 126)
                val textView = findTextView(view, text)
                assertTrue("Text must be present in the remote view", textView != null)
                assertNull("Text background must be transparent", textView?.background)
                assertEquals(0, bitmap.getPixel(0, 0))
                assertEquals(0, bitmap.getPixel(bitmap.width - 1, bitmap.height - 1))
                bitmap.recycle()
            }
        }
    }

    @Test
    fun waitingMessageUsesGridSpanWithoutChangingOtherMessages() {
        val deviceId = DeviceId(1, 1, 1)
        val cases = listOf(
            Triple(238, 30 to 12, R.string.text_short_waiting_for_data),
            Triple(478, 60 to 12, R.string.text_waiting_for_data),
            Triple(478, 30 to 12, R.string.text_short_waiting_for_data),
            Triple(638, 60 to 30, R.string.text_waiting_for_data),
            Triple(318, 30 to 30, R.string.text_short_waiting_for_data)
        )
        for (showLowestBatteryOnly in listOf(false, true)) {
            for ((width, gridSize, waitingTextId) in cases) {
                layoutWithBattery(width, showLowestBatteryOnly = showLowestBatteryOnly, gridSize = gridSize) { extensionView, view ->
                    val connectionConsumer = consumer<ConnectionInfo>(extensionView, "connectionInfoConsumer")
                    val message = view.findViewById<TextView>(R.id.textview_karoo_switch_batteries_message)
                    val waitingText = context.getString(waitingTextId)

                    repeat(2) {
                        connectionConsumer.accept(deviceId, ConnectionInfo(ConnectionStatus.CONNECTING))
                        assertEquals(View.VISIBLE, message.visibility)
                        assertEquals(waitingText, message.text.toString())
                        assertEquals(1, message.layout.lineCount)
                        assertEquals(0, message.layout.getEllipsisCount(0))
                        assertTrue(message.layout.getLineWidth(0) <= message.width - message.compoundPaddingLeft - message.compoundPaddingRight)
                        assertTrue(message.layout.height <= message.height)
                    }

                    connectionConsumer.accept(deviceId, ConnectionInfo(ConnectionStatus.ESTABLISHED))
                    assertEquals(View.INVISIBLE, message.visibility)
                    consumer<WirelessSwitchesInfo>(extensionView, "switchesConsumer").accept(
                        deviceId, WirelessSwitchesInfo(emptyList())
                    )
                    assertEquals(View.VISIBLE, message.visibility)
                    assertEquals(
                        context.getString(if (showLowestBatteryOnly) R.string.text_na else R.string.text_no_switches),
                        message.text.toString()
                    )

                    connectionConsumer.accept(deviceId, ConnectionInfo(ConnectionStatus.CLOSED))
                    assertEquals(
                        if (showLowestBatteryOnly) context.getString(R.string.text_na) else waitingText,
                        message.text.toString()
                    )
                }
            }
        }
    }

    @Test
    fun waitingMessageUpdatesWhenGridSpanChanges() {
        val deviceId = DeviceId(1, 1, 1)
        for (showLowestBatteryOnly in listOf(false, true)) {
            layoutWithBattery(showLowestBatteryOnly = showLowestBatteryOnly) { extensionView, _ ->
                consumer<ConnectionInfo>(extensionView, "connectionInfoConsumer").accept(
                    deviceId, ConnectionInfo(ConnectionStatus.CONNECTING)
                )
                for (columns in listOf(30, 60, 30, 60)) {
                    val view = extensionView.createView(
                        ViewConfig(gridSize = columns to 12, viewSize = 478 to 126, textSize = 30)
                    )
                    val message = view.findViewById<TextView>(R.id.textview_karoo_switch_batteries_message)
                    assertEquals(View.VISIBLE, message.visibility)
                    assertEquals(
                        context.getString(
                            if (columns == 30) R.string.text_short_waiting_for_data else R.string.text_waiting_for_data
                        ),
                        message.text.toString()
                    )
                }
            }
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

    private fun layoutWithBattery(
        width: Int = 478,
        height: Int = 126,
        showLowestBatteryOnly: Boolean = false,
        levels: List<WirelessSwitchBatteryLevel> = listOf(WirelessSwitchBatteryLevel.HIGH),
        gridSize: Pair<Int, Int> = 30 to 30,
        switches: List<WirelessSwitchInfo> = levels.mapIndexed { index, level ->
            switch(index + 1, SwitchType.TT_SWITCH, SwitchSide.NONE, level)
        },
        onCreated: (SwitchBatteriesExtensionView, View) -> Unit = { _, _ -> }
    ): View {
        val detachedContext = object : ContextWrapper(context) {
            override fun bindService(service: Intent, connection: ServiceConnection, flags: Int) = true
            override fun unbindService(connection: ServiceConnection) {}
        }
        val extensionContext = Ki2ExtensionContext("ki2", detachedContext)
        val extensionView = SwitchBatteriesExtensionView(extensionContext, showLowestBatteryOnly)
        try {
            val deviceId = DeviceId(1, 1, 1)
            consumer<ConnectionInfo>(extensionView, "connectionInfoConsumer").accept(
                deviceId, ConnectionInfo(ConnectionStatus.ESTABLISHED)
            )
            consumer<WirelessSwitchesInfo>(extensionView, "switchesConsumer").accept(
                deviceId, WirelessSwitchesInfo(switches)
            )
            val view = extensionView.createView(
                ViewConfig(
                    gridSize = gridSize,
                    viewSize = width to height,
                    textSize = 30,
                    alignment = ViewConfig.Alignment.RIGHT
                )
            )
            onCreated(extensionView, view)
            return view
        } finally {
            extensionView.dispose()
            extensionContext.serviceClient.dispose()
            extensionContext.karooSystem.disconnect()
        }
    }

    private fun assertTitleFits(title: TextView) {
        assertFalse(title.includeFontPadding)
        assertEquals(0.7f, title.lineSpacingMultiplier, 0f)
        assertTrue(title.layout.lineCount > 0)
        for (line in 0 until title.layout.lineCount) {
            assertEquals("The full header title must fit without truncation", 0, title.layout.getEllipsisCount(line))
            assertTrue(title.layout.getLineWidth(line) <= title.width)
        }
        assertTrue(title.layout.height <= title.height)
    }

    @Suppress("UNCHECKED_CAST")
    private fun <T> consumer(view: SwitchBatteriesExtensionView, name: String): BiConsumer<DeviceId, T> {
        return SwitchBatteriesExtensionView::class.java.getDeclaredField(name).apply {
            isAccessible = true
        }.get(view) as BiConsumer<DeviceId, T>
    }

    private fun findTextView(view: View, text: String): TextView? {
        if (view is TextView && view.text.toString() == text) return view
        if (view is ViewGroup) {
            for (index in 0 until view.childCount) {
                findTextView(view.getChildAt(index), text)?.let { return it }
            }
        }
        return null
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
