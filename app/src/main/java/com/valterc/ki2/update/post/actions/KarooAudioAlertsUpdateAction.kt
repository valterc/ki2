package com.valterc.ki2.update.post.actions

import androidx.core.content.edit
import androidx.preference.PreferenceManager
import com.valterc.ki2.R
import com.valterc.ki2.update.post.PostUpdateContext

class KarooAudioAlertsUpdateAction : IPreInitPostUpdateAction {
    override fun execute(context: PostUpdateContext) {
        val preferences = PreferenceManager.getDefaultSharedPreferences(context.context)

        val audioAlertPreferences = listOf(
            context.context.getString(R.string.preference_audio_alert_lowest_gear),
            context.context.getString(R.string.preference_audio_alert_highest_gear),
            context.context.getString(R.string.preference_audio_alert_shifting_limit),
            context.context.getString(R.string.preference_audio_alert_upcoming_synchro_shift)
        )

        for (preferenceKey in audioAlertPreferences) {
            val preferenceValue = preferences.getString(preferenceKey, null)

            if (preferenceValue == "karoo_workout_interval" || preferenceValue == "karoo_auto_lap") {
                preferences.edit {
                    putString(preferenceKey, "karoo_generic")
                }
            }
        }
    }
}