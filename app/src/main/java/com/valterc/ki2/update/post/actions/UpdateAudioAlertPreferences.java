package com.valterc.ki2.update.post.actions;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;

import androidx.preference.PreferenceManager;

import com.valterc.ki2.R;
import com.valterc.ki2.update.post.PostUpdateContext;

import timber.log.Timber;

public class UpdateAudioAlertPreferences implements IPreInitPostUpdateAction {

    @SuppressLint("ApplySharedPref")
    public void execute(PostUpdateContext context) {
        try {
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context.getContext());
            boolean audioAlertLowestGear = sharedPreferences.getBoolean(context.getContext().getString(R.string.preference_audio_alert_lowest_gear), false);
            boolean audioAlertHighestGear = sharedPreferences.getBoolean(context.getContext().getString(R.string.preference_audio_alert_highest_gear), false);
            boolean audioAlertShiftingLimit = sharedPreferences.getBoolean(context.getContext().getString(R.string.preference_audio_alert_shifting_limit), false);
            boolean audioAlertSynchroShift = sharedPreferences.getBoolean(context.getContext().getString(R.string.preference_audio_alert_upcoming_synchro_shift), false);

            SharedPreferences.Editor editor = sharedPreferences.edit();

            editor.putString(context.getContext().getString(R.string.preference_audio_alert_lowest_gear),
                    audioAlertLowestGear ? "karoo_auto_lap" : context.getContext().getString(R.string.default_preference_audio_alert_shifting_limit));
            editor.putString(context.getContext().getString(R.string.preference_audio_alert_highest_gear),
                    audioAlertHighestGear ? "karoo_auto_lap" : context.getContext().getString(R.string.default_preference_audio_alert_shifting_limit));
            editor.putString(context.getContext().getString(R.string.preference_audio_alert_shifting_limit),
                    audioAlertShiftingLimit ? "karoo_auto_lap" : context.getContext().getString(R.string.default_preference_audio_alert_shifting_limit));
            editor.putString(context.getContext().getString(R.string.preference_audio_alert_upcoming_synchro_shift),
                    audioAlertSynchroShift ? "karoo_workout_interval" : context.getContext().getString(R.string.default_preference_audio_alert_shifting_limit));

            editor.commit();
        } catch (Exception e) {
            Timber.e(e, "Unable to update audio alert preferences");
        }
    }

}
