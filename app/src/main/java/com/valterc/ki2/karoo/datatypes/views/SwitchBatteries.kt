package com.valterc.ki2.karoo.datatypes.views

import android.graphics.Bitmap
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceModifier
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.appwidget.background
import androidx.glance.color.ColorProvider
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.ContentScale
import androidx.glance.layout.Row
import androidx.glance.layout.fillMaxHeight
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.padding
import androidx.glance.layout.width
import androidx.glance.text.FontFamily
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import com.valterc.ki2.data.switches.SwitchSide
import io.hammerhead.karooext.models.ViewConfig

data class SwitchBatteryItem(val side: SwitchSide, val battery: Bitmap)

data class SwitchBatteryRow(val typeName: String?, val items: List<SwitchBatteryItem>)

@Composable
fun SwitchBatteries(
    rows: List<SwitchBatteryRow>,
    typeNameWidthDp: Int,
    fontSize: Int = 13,
) {
    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .padding(5.dp, 0.dp),
    ) {
        rows.forEach { row ->
            Row(
                modifier = GlanceModifier
                    .fillMaxWidth()
                    .defaultWeight(),
                verticalAlignment = Alignment.Vertical.CenterVertically,
            ) {
                row.typeName?.let { typeName ->
                    Text(
                        text = typeName,
                        style = TextStyle(
                            ColorProvider(Color.Black, Color.White),
                            fontSize = fontSize.sp,
                            fontFamily = FontFamily.SansSerif
                        ),
                        maxLines = 1,
                        modifier = GlanceModifier
                            .width(typeNameWidthDp.dp)
                    )
                }

                getColumns(row).forEach { side ->
                    Row(
                        modifier = GlanceModifier
                            .defaultWeight()
                            .fillMaxHeight(),
                        horizontalAlignment = Alignment.Horizontal.CenterHorizontally,
                        verticalAlignment = Alignment.Vertical.CenterVertically,
                    ) {
                        row.items.filter { it.side == side }.forEach { item ->
                            Box(
                                modifier = GlanceModifier
                                    .defaultWeight()
                                    .fillMaxHeight()
                                    .padding(4.dp, 3.dp),
                                contentAlignment = Alignment.Center,
                            ) {
                                Image(
                                    provider = ImageProvider(item.battery),
                                    contentDescription = null,
                                    contentScale = ContentScale.Fit,
                                    modifier = GlanceModifier.fillMaxSize()
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun getColumns(row: SwitchBatteryRow): List<SwitchSide> {
    val hasSides = row.items.any { it.side == SwitchSide.LEFT || it.side == SwitchSide.RIGHT }
    val hasCenter = row.items.any { it.side == SwitchSide.NONE }

    return when {
        hasSides && hasCenter -> listOf(SwitchSide.LEFT, SwitchSide.NONE, SwitchSide.RIGHT)
        hasSides -> listOf(SwitchSide.LEFT, SwitchSide.RIGHT)
        else -> listOf(SwitchSide.NONE)
    }
}

@Composable
fun SwitchBatteriesMessage(
    text: String,
    dataAlignment: ViewConfig.Alignment = ViewConfig.Alignment.RIGHT,
    fontSize: Int = 20
) {
    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .padding(5.dp, 0.dp),
        contentAlignment = Alignment(
            vertical = Alignment.Vertical.CenterVertically,
            horizontal = when (dataAlignment) {
                ViewConfig.Alignment.LEFT -> Alignment.Horizontal.Start
                ViewConfig.Alignment.CENTER,
                ViewConfig.Alignment.RIGHT,
                    -> Alignment.Horizontal.End
            },
        ),
    ) {
        Text(
            text = text,
            style = TextStyle(
                ColorProvider(Color.Black, Color.White),
                fontSize = fontSize.sp,
                fontFamily = FontFamily.SansSerif
            ),
            modifier = GlanceModifier
                .background(Color(1f, 1f, 1f, 1f), Color(0f, 0f, 0f, 1f))
        )
    }
}
