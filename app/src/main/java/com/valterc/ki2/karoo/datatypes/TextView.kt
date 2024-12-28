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
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.padding
import androidx.glance.preview.ExperimentalGlancePreviewApi
import androidx.glance.preview.Preview
import androidx.glance.text.FontFamily
import androidx.glance.text.Text
import androidx.glance.text.TextStyle

@OptIn(ExperimentalGlancePreviewApi::class)
@Preview(widthDp = 100, heightDp = 50)
@Composable
fun TextView(text: String? = null, fontSize: Int = 14) {
    Box(
        modifier = GlanceModifier.fillMaxSize().background(Color(1f, 1f, 1f, 0.4f), Color(0f, 0f, 0f, 0.4f)),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text ?: "",
            style = TextStyle(ColorProvider(Color.Black, Color.White), fontSize = fontSize.sp, fontFamily = FontFamily.SansSerif),
            modifier = GlanceModifier.padding(1.dp)
        )
    }
}