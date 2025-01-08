package com.valterc.ki2.karoo.audio

import com.valterc.ki2.data.message.AudioAlertMessage
import com.valterc.ki2.data.preferences.PreferencesView
import com.valterc.ki2.karoo.Ki2ExtensionContext
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
    private var frequencyMultiplier: Double = 1.0

    private val playAudioConsumer = Consumer<AudioAlertMessage> {
        playAudio(it.name)
    }

    private val preferencesConsumer = Consumer<PreferencesView> { preferences ->
        enableAudioAlerts = preferences.isAudioAlertsEnabled(context.context)
        audioAlertLowestGear = preferences.getAudioAlertLowestGear(context.context)
        audioAlertHighestGear = preferences.getAudioAlertHighestGear(context.context)
        audioAlertShiftingLimit = preferences.getAudioAlertShiftingLimit(context.context)
        audioAlertUpcomingSynchroShift = preferences.getAudioAlertUpcomingSynchroShift(context.context)
        delayBetweenAlerts = preferences.getDelayBetweenAudioAlerts(context.context)
        frequencyMultiplier = preferences.getAudioAlertFrequencyMultiplier(context.context)
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

    private fun adjustFrequency(frequency: Int): Int {
        return (frequency * frequencyMultiplier).toInt()
    }

    private fun adjustDuration(duration: Int): Int {
        if (frequencyMultiplier < 0.1) {
           return max((duration * 0.7).toInt(), 50)
        }

        return duration
    }

    fun playSingleBeep() {
        context.karooSystem.dispatch(
            PlayBeepPattern(listOf(PlayBeepPattern.Tone(adjustFrequency(5000), adjustDuration(350)))
            )
        )
    }

    fun playDoubleBeep() {
        context.karooSystem.dispatch(
            PlayBeepPattern(
                listOf(
                    PlayBeepPattern.Tone(adjustFrequency(5000), adjustDuration(350)),
                    PlayBeepPattern.Tone(null, 150),
                    PlayBeepPattern.Tone(adjustFrequency(5000), adjustDuration(350))
                )
            )
        )
    }

    private fun playKarooWorkoutInterval() {
        context.karooSystem.dispatch(
            PlayBeepPattern(
                listOf(
                    PlayBeepPattern.Tone(adjustFrequency(5000), adjustDuration(350)),
                    PlayBeepPattern.Tone(null, 100),
                    PlayBeepPattern.Tone(adjustFrequency(4250), adjustDuration(100)),
                    PlayBeepPattern.Tone(null, 50),
                    PlayBeepPattern.Tone(adjustFrequency(4250), adjustDuration(100)),
                    PlayBeepPattern.Tone(null, 50),
                    PlayBeepPattern.Tone(adjustFrequency(4250), adjustDuration(100))
                )
            )
        )
    }

    private fun playKarooAutoLap() {
        context.karooSystem.dispatch(
            PlayBeepPattern(
                listOf(
                    PlayBeepPattern.Tone(adjustFrequency(5000), adjustDuration(350)),
                    PlayBeepPattern.Tone(null, 100),
                    PlayBeepPattern.Tone(adjustFrequency(4250), adjustDuration(100)),
                    PlayBeepPattern.Tone(null, 50),
                    PlayBeepPattern.Tone(adjustFrequency(4250), adjustDuration(100)),
                    PlayBeepPattern.Tone(null, 50),
                    PlayBeepPattern.Tone(adjustFrequency(4250), adjustDuration(100))
                )
            )
        )
    }

    fun playKarooBell() {
        context.karooSystem.dispatch(
            PlayBeepPattern(
                listOf(
                    PlayBeepPattern.Tone(adjustFrequency(3750), adjustDuration(50)),
                    PlayBeepPattern.Tone(adjustFrequency(5800), adjustDuration(200)),
                    PlayBeepPattern.Tone(null, 50),
                    PlayBeepPattern.Tone(adjustFrequency(3750), adjustDuration(50)),
                    PlayBeepPattern.Tone(adjustFrequency(5800), adjustDuration(300))
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