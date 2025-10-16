package com.valterc.ki2.update.post.actions

import androidx.core.content.edit
import androidx.preference.PreferenceManager
import com.valterc.ki2.R
import com.valterc.ki2.update.post.PostUpdateContext

class KarooBellUpdateAction : IPreInitPostUpdateAction {
    override fun execute(context: PostUpdateContext) {
        val preferences = PreferenceManager.getDefaultSharedPreferences(context.context)

        val preferencesWithPossibleBellValue = listOf(
            context.context.getString(R.string.preference_audio_alert_lowest_gear),
            context.context.getString(R.string.preference_audio_alert_highest_gear),
            context.context.getString(R.string.preference_audio_alert_shifting_limit),
            context.context.getString(R.string.preference_audio_alert_upcoming_synchro_shift),
            context.context.getString(R.string.preference_switch_ch1_single_press),
            context.context.getString(R.string.preference_switch_ch1_double_press),
            context.context.getString(R.string.preference_switch_ch1_hold),
            context.context.getString(R.string.preference_switch_ch2_single_press),
            context.context.getString(R.string.preference_switch_ch2_double_press),
            context.context.getString(R.string.preference_switch_ch2_hold),
            context.context.getString(R.string.preference_switch_ch3_single_press),
            context.context.getString(R.string.preference_switch_ch3_double_press),
            context.context.getString(R.string.preference_switch_ch3_hold),
            context.context.getString(R.string.preference_switch_ch4_single_press),
            context.context.getString(R.string.preference_switch_ch4_double_press),
            context.context.getString(R.string.preference_switch_ch4_hold),
        )

        for (preferenceKey in preferencesWithPossibleBellValue) {
            val preferenceValue = preferences.getString(preferenceKey, null)

            when (preferenceValue) {
                "press_bell" -> preferences.edit {
                    putString(preferenceKey, "press_bell_old")
                }
                "hold_short_single_bell" -> preferences.edit {
                    putString(preferenceKey, "hold_short_single_bell_old")
                }
                "hold_continuous_bell" -> preferences.edit {
                    putString(preferenceKey, "hold_continuous_bell_old")
                }
                "karoo_bell" -> preferences.edit {
                    putString(preferenceKey, "karoo_bell_old")
                }
            }
        }
    }
}