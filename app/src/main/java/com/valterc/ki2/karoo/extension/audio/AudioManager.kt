package com.valterc.ki2.karoo.extension.audio

import com.valterc.ki2.data.message.AudioAlertMessage
import com.valterc.ki2.data.preferences.PreferencesView
import com.valterc.ki2.karoo.extension.Ki2ExtensionContext
import io.hammerhead.karooext.models.HardwareType
import io.hammerhead.karooext.models.PlayBeepPattern
import timber.log.Timber
import java.util.function.Consumer

class AudioManager(private val context: Ki2ExtensionContext) {

    private var enableAudioAlerts: Boolean = true;
    private var audioAlertLowestGear: String? = null
    private var audioAlertHighestGear: String? = null
    private var audioAlertShiftingLimit: String? = null
    private var audioAlertUpcomingSynchroShift: String? = null
    private var delayBetweenAlerts: Int = 0
    private var timestampLastAlert: Long = 0

    private val playAudioConsumer = Consumer<AudioAlertMessage> {
        playAudio(it.name)
    }

    private val preferencesConsumer = Consumer<PreferencesView> {
        enableAudioAlerts = it.isAudioAlertsEnabled(context.context)
        audioAlertLowestGear = it.getAudioAlertLowestGear(context.context)
        audioAlertHighestGear = it.getAudioAlertHighestGear(context.context)
        audioAlertShiftingLimit = it.getAudioAlertShiftingLimit(context.context)
        audioAlertUpcomingSynchroShift = it.getAudioAlertUpcomingSynchroShift(context.context)
        delayBetweenAlerts = it.getDelayBetweenAudioAlerts(context.context)
    }

    init {
        context.serviceClient.customMessageClient.registerAudioAlertWeakListener(playAudioConsumer)
        context.serviceClient.registerPreferencesWeakListener(preferencesConsumer)
    }

    private fun playAudio(audio: String?) {
        when (audio) {
            "karoo_workout_interval" -> playKarooWorkoutInterval()
            "karoo_auto_lap" -> playKarooAutoLap()
            "karoo_bell" -> playKarooBell()
            "custom_single_beep" -> playSingleBeep()
            "custom_double_beep" -> playDoubleBeep()
            "disabled" -> return
            else -> Timber.i("Unknown audio requested '%s'", audio)
        }
    }

    fun playSingleBeep() {
        context.karooSystem.dispatch(
            PlayBeepPattern(
                listOf(PlayBeepPattern.Tone(5000, 350))
            )
        )
    }

    fun playDoubleBeep() {
        context.karooSystem.dispatch(
            PlayBeepPattern(
                listOf(
                    PlayBeepPattern.Tone(5000, 350),
                    PlayBeepPattern.Tone(null, 200),
                    PlayBeepPattern.Tone(5000, 350)
                )
            )
        )
    }

    fun playKarooWorkoutInterval() {
        when (context.karooSystem.hardwareType) {
            HardwareType.K2 -> context.karooSystem.dispatch(
                PlayBeepPattern(
                    listOf(
                        PlayBeepPattern.Tone(5000, 350),
                        PlayBeepPattern.Tone(null, 100),
                        PlayBeepPattern.Tone(4250, 100),
                        PlayBeepPattern.Tone(null, 50),
                        PlayBeepPattern.Tone(4250, 100),
                        PlayBeepPattern.Tone(null, 50),
                        PlayBeepPattern.Tone(4250, 100)
                    )
                )
            )
            else -> context.karooSystem.dispatch(
                PlayBeepPattern(
                    listOf(
                        PlayBeepPattern.Tone(5000, 350),
                        PlayBeepPattern.Tone(null, 100),
                        PlayBeepPattern.Tone(4250, 100),
                        PlayBeepPattern.Tone(null, 50),
                        PlayBeepPattern.Tone(4250, 100),
                        PlayBeepPattern.Tone(null, 50),
                        PlayBeepPattern.Tone(4250, 100)
                    )
                )
            )
        }
    }

    fun playKarooAutoLap() {
        when (context.karooSystem.hardwareType) {
            HardwareType.K2 -> context.karooSystem.dispatch(
                PlayBeepPattern(
                    listOf(
                        PlayBeepPattern.Tone(5000, 350),
                        PlayBeepPattern.Tone(null, 100),
                        PlayBeepPattern.Tone(4250, 100),
                        PlayBeepPattern.Tone(null, 50),
                        PlayBeepPattern.Tone(4250, 100),
                        PlayBeepPattern.Tone(null, 50),
                        PlayBeepPattern.Tone(4250, 100)
                    )
                )
            )
            else -> context.karooSystem.dispatch(
                PlayBeepPattern(
                    listOf(
                        PlayBeepPattern.Tone(2959, 125),
                        PlayBeepPattern.Tone(null, 187),
                        PlayBeepPattern.Tone(1975, 62),
                        PlayBeepPattern.Tone(null, 62),
                        PlayBeepPattern.Tone(1975, 62)
                    )
                )
            )
        }
    }

    fun playKarooBell() {
        when (context.karooSystem.hardwareType) {
            HardwareType.K2 -> context.karooSystem.dispatch(
                PlayBeepPattern(
                    listOf(
                        PlayBeepPattern.Tone(3750, 50),
                        PlayBeepPattern.Tone(5800, 200),
                        PlayBeepPattern.Tone(null, 50),
                        PlayBeepPattern.Tone(3750, 50),
                        PlayBeepPattern.Tone(5800, 300)
                    )
                )
            )
            else -> context.karooSystem.dispatch(
                PlayBeepPattern(
                    listOf(
                        PlayBeepPattern.Tone(2489, 114),
                        PlayBeepPattern.Tone(2959, 114),
                        PlayBeepPattern.Tone(2489, 114),
                        PlayBeepPattern.Tone(2959, 114),
                        PlayBeepPattern.Tone(null, 135),
                        PlayBeepPattern.Tone(2489, 114),
                        PlayBeepPattern.Tone(2959, 114),
                        PlayBeepPattern.Tone(2489, 114),
                        PlayBeepPattern.Tone(2959, 114)
                    )
                )
            )
        }
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