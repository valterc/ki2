package com.valterc.ki2.karoo.datatypes.views

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceModifier
import androidx.glance.appwidget.background
import androidx.glance.color.ColorProvider
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.padding
import androidx.glance.preview.ExperimentalGlancePreviewApi
import androidx.glance.preview.Preview
import androidx.glance.text.FontFamily
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import io.hammerhead.karooext.models.ViewConfig

@OptIn(ExperimentalGlancePreviewApi::class)
@Preview(widthDp = 100, heightDp = 50)
@Composable
fun TextView(
    text: String? = "",
    dataAlignment: ViewConfig.Alignment = ViewConfig.Alignment.RIGHT,
    fontSize: Int = 50
) {
    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .padding(start = 5.dp, top = 0.dp, end = 5.dp, bottom = 5.dp),
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
            text = text ?: "",
            style = TextStyle(
                ColorProvider(Color.Black, Color.White),
                fontSize = (fontSize * 0.97).sp,
                fontFamily = FontFamily.Monospace
            ),
            modifier = GlanceModifier
                .background(Color(1f, 1f, 1f, 1f), Color(0f, 0f, 0f, 1f))
                .padding(top = (-fontSize * 0.09).dp)
        )
    }
}