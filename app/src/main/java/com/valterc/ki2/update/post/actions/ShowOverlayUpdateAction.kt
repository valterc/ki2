package com.valterc.ki2.update.post.actions

import androidx.core.content.edit
import androidx.preference.PreferenceManager
import com.valterc.ki2.R
import com.valterc.ki2.update.post.PostUpdateContext

class ShowOverlayUpdateAction : IPreInitPostUpdateAction {
    override fun execute(context: PostUpdateContext) {
        val preferences = PreferenceManager.getDefaultSharedPreferences(context.context)

        val switchPressPreferences = listOf(
            context.context.getString(R.string.preference_switch_ch1_single_press),
            context.context.getString(R.string.preference_switch_ch1_double_press),

            context.context.getString(R.string.preference_switch_ch2_single_press),
            context.context.getString(R.string.preference_switch_ch2_double_press),

            context.context.getString(R.string.preference_switch_ch3_single_press),
            context.context.getString(R.string.preference_switch_ch3_double_press),

            context.context.getString(R.string.preference_switch_ch4_single_press),
            context.context.getString(R.string.preference_switch_ch4_double_press),
        )

        val switchHoldPreferences = listOf(
            context.context.getString(R.string.preference_switch_ch1_hold),
            context.context.getString(R.string.preference_switch_ch2_hold),
            context.context.getString(R.string.preference_switch_ch3_hold),
            context.context.getString(R.string.preference_switch_ch4_hold),
        )

        for (preferenceKey in switchPressPreferences) {
            val preferenceValue = preferences.getString(preferenceKey, null)

            if (preferenceValue == "show_overlay") {
                preferences.edit {
                    putString(preferenceKey, "press_show_overlay")
                }
            }
        }

        for (preferenceKey in switchHoldPreferences) {
            val preferenceValue = preferences.getString(preferenceKey, null)

            if (preferenceValue == "show_overlay") {
                preferences.edit {
                    putString(preferenceKey, "hold_short_single_show_overlay")
                }
            }
        }
    }
}