package com.valterc.ki2.karoo.audio

import com.valterc.ki2.data.message.AudioAlertMessage
import com.valterc.ki2.data.preferences.PreferencesView
import com.valterc.ki2.karoo.Ki2ExtensionContext
import io.hammerhead.karooext.models.HardwareType
import io.hammerhead.karooext.models.PlayBeepPattern
import timber.log.Timber
import java.util.function.Consumer
import kotlin.math.max

class AudioManager(private val context: Ki2ExtensionContext) {

    private var enableAudioAlerts: Boolean = true
    private var audioAlertLowestGear: String? = null
    private var audioAlertHighestGear: String? = null
    private var audioAlertShiftingLimit: String? = null
    private var audioAlertUpcomingSynchroShift: String? = null
    private var delayBetweenAlerts: Int = 0
    private var timestampLastAlert: Long = 0
    private var intensity: AudioIntensity = AudioIntensity.Normal

    private val playAudioConsumer = Consumer<AudioAlertMessage> {
        playAudio(it.name)
    }

    private val preferencesConsumer = Consumer<PreferencesView> { preferences ->
        enableAudioAlerts = preferences.isAudioAlertsEnabled(context.context)
        audioAlertLowestGear = preferences.getAudioAlertLowestGear(context.context)
        audioAlertHighestGear = preferences.getAudioAlertHighestGear(context.context)
        audioAlertShiftingLimit = preferences.getAudioAlertShiftingLimit(context.context)
        audioAlertUpcomingSynchroShift =
            preferences.getAudioAlertUpcomingSynchroShift(context.context)
        delayBetweenAlerts = preferences.getDelayBetweenAudioAlerts(context.context)
        intensity = preferences.getAudioAlertIntensity(context.context)
    }

    init {
        context.serviceClient.customMessageClient.registerAudioAlertWeakListener(playAudioConsumer)
        context.serviceClient.registerPreferencesWeakListener(preferencesConsumer)
    }

    private fun playAudio(audio: String?) {
        when (audio) {
            "karoo_generic" -> playKarooGeneric()
            "karoo_bell" -> playKarooBell()
            "custom_single_beep" -> playSingleBeep()
            "custom_double_beep" -> playDoubleBeep()
            "disabled" -> return
            else -> Timber.i("Unknown audio requested '%s'", audio)
        }
    }

    private val Int.f: Int
        get() = when (context.karooSystem.hardwareType) {
            HardwareType.K2 -> times(intensity.frequencyMultiplierK2).toInt()
            else -> times(intensity.frequencyMultiplierK24).toInt()
        }

    private val Int.d: Int
        get() = when (context.karooSystem.hardwareType) {
            HardwareType.K2 -> max(times(intensity.durationMultiplierK2).toInt(), 50)
            else -> max(times(intensity.durationMultiplierK24).toInt(), 50)
        }

    fun playSingleBeep(adjustVolume: Boolean = true) {
        if (adjustVolume){
            context.karooSystem.dispatch(
                PlayBeepPattern(
                    listOf(
                        PlayBeepPattern.Tone(5000.f, 350.d)
                    )
                )
            )
        } else {
            context.karooSystem.dispatch(
                PlayBeepPattern(
                    listOf(
                        PlayBeepPattern.Tone(5000, 350)
                    )
                )
            )
        }
    }

    fun playDoubleBeep(adjustVolume: Boolean = true) {
        if (adjustVolume) {
            context.karooSystem.dispatch(
                PlayBeepPattern(
                    listOf(
                        PlayBeepPattern.Tone(5000.f, 350.d),
                        PlayBeepPattern.Tone(null, 150.d),
                        PlayBeepPattern.Tone(5000.f, 350.d)
                    )
                )
            )
        } else {
            context.karooSystem.dispatch(
                PlayBeepPattern(
                    listOf(
                        PlayBeepPattern.Tone(5000, 350),
                        PlayBeepPattern.Tone(null, 150),
                        PlayBeepPattern.Tone(5000, 350)
                    )
                )
            )
        }
    }

    private fun playKarooGeneric() {
        context.karooSystem.dispatch(
            PlayBeepPattern(
                listOf(
                    PlayBeepPattern.Tone(5000.f, 350.d),
                    PlayBeepPattern.Tone(null, 100.d),
                    PlayBeepPattern.Tone(4250.f, 100.d),
                    PlayBeepPattern.Tone(null, 50.d),
                    PlayBeepPattern.Tone(4250.f, 100.d),
                    PlayBeepPattern.Tone(null, 50.d),
                    PlayBeepPattern.Tone(4250.f, 100.d)
                )
            )
        )
    }

    fun playKarooBell(adjustVolume: Boolean = true) {
        if (adjustVolume){
            context.karooSystem.dispatch(
                PlayBeepPattern(
                    listOf(
                        PlayBeepPattern.Tone(2349.f, 31.d),
                        PlayBeepPattern.Tone(3520.f, 125.d),
                        PlayBeepPattern.Tone(null, 31.d),
                        PlayBeepPattern.Tone(2349.f, 31.d),
                        PlayBeepPattern.Tone(3520.f, 406.d)
                    )
                )
            )
        } else {
            context.karooSystem.dispatch(
                PlayBeepPattern(
                    listOf(
                        PlayBeepPattern.Tone(2349, 31),
                        PlayBeepPattern.Tone(3520, 125),
                        PlayBeepPattern.Tone(null, 31),
                        PlayBeepPattern.Tone(2349, 31),
                        PlayBeepPattern.Tone(3520, 406)
                    )
                )
            )
        }
    }

    fun playKarooDeviceWarning() {
        context.karooSystem.dispatch(
            PlayBeepPattern(
                listOf(
                    PlayBeepPattern.Tone(1975.f, 125.d),
                    PlayBeepPattern.Tone(null, 125.d),
                    PlayBeepPattern.Tone(1975.f, 125.d),
                    PlayBeepPattern.Tone(null, 125.d),
                    PlayBeepPattern.Tone(1975.f, 125.d)
                )
            )
        )
    }

    fun playLowestGearAudioAlert() {
        tryTriggerAudioAlert {
            playAudio(audioAlertLowestGear)
        }
    }

    fun playHighestGearAudioAlert() {
        tryTriggerAudioAlert {
            playAudio(audioAlertHighestGear)
        }
    }

    fun playShiftingLimitAudioAlert() {
        tryTriggerAudioAlert {
            playAudio(audioAlertShiftingLimit)
        }
    }

    fun playUpcomingSynchroShiftAudioAlert() {
        tryTriggerAudioAlert {
            playAudio(audioAlertUpcomingSynchroShift)
        }
    }

    private fun tryTriggerAudioAlert(audioTrigger: Runnable) {
        if (!enableAudioAlerts) {
            return
        }

        if (System.currentTimeMillis() - timestampLastAlert > delayBetweenAlerts) {
            audioTrigger.run()
            timestampLastAlert = System.currentTimeMillis()
        }
    }

}