package com.valterc.ki2.karoo

import android.content.Context
import com.valterc.ki2.input.ActionManager
import com.valterc.ki2.karoo.audio.AudioManager
import com.valterc.ki2.karoo.service.ServiceClient
import io.hammerhead.karooext.KarooSystemService


class Ki2ExtensionContext(val context: Context) {
    val karooSystem: KarooSystemService = KarooSystemService(context)
    val serviceClient: ServiceClient = ServiceClient(context)
    val audioManager: AudioManager = AudioManager(this)
    val actionManager: ActionManager = ActionManager(this)
}