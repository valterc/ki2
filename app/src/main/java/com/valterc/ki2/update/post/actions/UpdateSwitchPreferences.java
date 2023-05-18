package com.valterc.ki2.update.post.actions;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;

import androidx.preference.PreferenceManager;

import com.valterc.ki2.R;
import com.valterc.ki2.update.post.PostUpdateContext;

import timber.log.Timber;

public class UpdateSwitchPreferences implements IPreInitPostUpdateAction {

    @SuppressLint("ApplySharedPref")
    public void execute(PostUpdateContext context) {
        try {
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context.getContext());
            String leftSwitchSinglePress = sharedPreferences.getString("LEFT_SWITCH_SINGLE_PRESS", null);
            String leftSwitchDoublePress = sharedPreferences.getString("LEFT_SWITCH_DOUBLE_PRESS", null);
            String leftSwitchHold = sharedPreferences.getString("LEFT_SWITCH_HOLD", null);
            String rightSwitchSinglePress = sharedPreferences.getString("RIGHT_SWITCH_SINGLE_PRESS", null);
            String rightSwitchDoublePress = sharedPreferences.getString("RIGHT_SWITCH_DOUBLE_PRESS", null);
            String rightSwitchHold = sharedPreferences.getString("RIGHT_SWITCH_HOLD", null);

            SharedPreferences.Editor editor = sharedPreferences.edit();

            setPreference(context, editor, R.string.preference_switch_ch1_single_press, leftSwitchSinglePress);
            setPreference(context, editor, R.string.preference_switch_ch1_double_press, leftSwitchDoublePress);
            setPreference(context, editor, R.string.preference_switch_ch1_hold, leftSwitchHold);
            setPreference(context, editor, R.string.preference_switch_ch2_single_press, rightSwitchSinglePress);
            setPreference(context, editor, R.string.preference_switch_ch2_double_press, rightSwitchDoublePress);
            setPreference(context, editor, R.string.preference_switch_ch2_hold, rightSwitchHold);

            editor.commit();
        } catch (Exception e) {
            Timber.e(e, "Unable to update switch preferences");
        }
    }

    private static void setPreference(PostUpdateContext context, SharedPreferences.Editor editor, int newPreferenceResource, String value) {
        if (value != null) {
            editor.putString(context.getContext().getString(newPreferenceResource), value);
        }
    }
}
