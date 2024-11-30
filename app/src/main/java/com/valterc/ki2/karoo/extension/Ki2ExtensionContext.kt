package com.valterc.ki2.karoo.extension

import android.content.Context
import com.valterc.ki2.karoo.extension.audio.AudioManager
import com.valterc.ki2.karoo.service.ServiceClient
import io.hammerhead.karooext.KarooSystemService


class Ki2ExtensionContext(val context: Context) {
    val karooSystem: KarooSystemService = KarooSystemService(context)
    val serviceClient: ServiceClient = ServiceClient(context)
    val audioManager: AudioManager = AudioManager(this)
}