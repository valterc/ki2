package com.valterc.ki2.karoo.extension.overlay

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Context.WINDOW_SERVICE
import android.content.Intent
import android.graphics.PixelFormat
import android.provider.Settings
import android.view.Gravity
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.WindowManager
import androidx.core.app.NotificationCompat
import com.valterc.ki2.R
import com.valterc.ki2.karoo.extension.Ki2ExtensionContext
import com.valterc.ki2.karoo.overlay.manager.OverlayManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@SuppressLint("InflateParams")
class OverlayWindow(
    private val extensionContext: Ki2ExtensionContext
) {
    private var overlayManager: OverlayManager? = null

    fun open() {
        if (!Settings.canDrawOverlays(extensionContext.context)) {
            return
        }

        if (overlayManager == null) {
            overlayManager = OverlayManager(extensionContext)
        }
    }

    fun close() {
        overlayManager = null
    }
}