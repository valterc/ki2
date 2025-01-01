package com.valterc.ki2.karoo.datatypes

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceModifier
import androidx.glance.appwidget.background
import androidx.glance.color.ColorProvider
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.padding
import androidx.glance.layout.wrapContentHeight
import androidx.glance.preview.ExperimentalGlancePreviewApi
import androidx.glance.preview.Preview
import androidx.glance.text.FontFamily
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import io.hammerhead.karooext.models.ViewConfig

@OptIn(ExperimentalGlancePreviewApi::class)
@Preview(widthDp = 100, heightDp = 50)
@Composable
fun TextView(text: String? = null, dataAlignment: ViewConfig.Alignment, fontSize: Int = 14) {
    Box(
        modifier = GlanceModifier
            .fillMaxWidth()
            .wrapContentHeight()
            .background(Color(1f, 1f, 1f, 1f), Color(0f, 0f, 0f, 1f)),
        contentAlignment = Alignment(
            vertical = Alignment.Vertical.CenterVertically,
            horizontal = when (dataAlignment) {
                ViewConfig.Alignment.LEFT -> Alignment.Horizontal.Start
                ViewConfig.Alignment.CENTER,
                ViewConfig.Alignment.RIGHT, -> Alignment.Horizontal.End
            },
        ),
    ) {
        Text(
            text = text ?: "",
            style = TextStyle(ColorProvider(Color.Black, Color.White), fontSize = fontSize.sp, fontFamily = FontFamily.SansSerif),
            modifier = GlanceModifier.padding(1.dp).padding(top = (-20).dp)
        )
    }
}