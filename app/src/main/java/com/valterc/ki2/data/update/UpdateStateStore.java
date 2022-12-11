package com.valterc.ki2.data.update;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.preference.PreferenceManager;

import com.valterc.ki2.R;

import java.time.Instant;
import java.time.Period;

public final class UpdateStateStore {

    private static final String PREFERENCE_NAME = "UpdateState";
    private static final String PREFERENCE_KEY_ONGOING_UPDATE = "OngoingUpdate";
    private static final String PREFERENCE_KEY_PREVIOUS_VERSION = "PreviousVersion";
    private static final String PREFERENCE_KEY_NEW_VERSION = "NewVersion";
    private static final String PREFERENCE_KEY_UPDATE_INSTANT = "UpdateInstant";
    private static final String PREFERENCE_KEY_CHECK_INSTANT = "CheckInstant";
    private static final String PREFERENCE_KEY_FAILED = "LastCheckInstant";
    private static final String PREFERENCE_KEY_UPDATE_AVAILABLE = "UpdateAvailable";
    private static final String PREFERENCE_KEY_UPDATE_VERSION = "UpdateVersion";

    private UpdateStateStore() {
    }

    @SuppressLint("ApplySharedPref")
    public static void willUpdate(Context context, String previousVersion, String newVersion) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(PREFERENCE_KEY_PREVIOUS_VERSION, previousVersion);
        editor.putString(PREFERENCE_KEY_NEW_VERSION, newVersion);
        editor.putLong(PREFERENCE_KEY_UPDATE_INSTANT, Instant.now().toEpochMilli());
        editor.putBoolean(PREFERENCE_KEY_ONGOING_UPDATE, true);
        editor.putBoolean(PREFERENCE_KEY_FAILED, false);
        editor.putBoolean(PREFERENCE_KEY_UPDATE_AVAILABLE, false);
        editor.putString(PREFERENCE_KEY_UPDATE_VERSION, null);
        editor.commit();
    }

    public static void updateFailed(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(PREFERENCE_KEY_ONGOING_UPDATE, false);
        editor.putBoolean(PREFERENCE_KEY_FAILED, false);
        editor.apply();
    }

    @SuppressLint("ApplySharedPref")
    @Nullable
    public static OngoingUpdateStateInfo getAndClearOngoingUpdateState(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE);
        if (!sharedPreferences.getBoolean(PREFERENCE_KEY_ONGOING_UPDATE, false)) {
            return null;
        }

        OngoingUpdateStateInfo ongoingUpdateStateInfo = new OngoingUpdateStateInfo(
                sharedPreferences.getString(PREFERENCE_KEY_PREVIOUS_VERSION, null),
                sharedPreferences.getString(PREFERENCE_KEY_NEW_VERSION, null),
                Instant.ofEpochMilli(sharedPreferences.getLong(PREFERENCE_KEY_UPDATE_INSTANT, 0))
        );

        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove(PREFERENCE_KEY_ONGOING_UPDATE);
        editor.commit();

        return ongoingUpdateStateInfo;
    }

    public static boolean isUpdateOngoing(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getBoolean(PREFERENCE_KEY_ONGOING_UPDATE, false);
    }

    public static boolean shouldAutomaticallyCheckForUpdatesInApp(Context context) {
        SharedPreferences defaultSharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE);

        return defaultSharedPreferences.getBoolean(context.getString(R.string.preference_auto_update), true) &&
                (sharedPreferences.getBoolean(PREFERENCE_KEY_UPDATE_AVAILABLE, false) ||
                        Instant.ofEpochMilli(sharedPreferences.getLong(PREFERENCE_KEY_CHECK_INSTANT, 0))
                                .plus(Period.ofDays(1)).isBefore(Instant.now()));
    }

    public static boolean shouldAutomaticallyCheckForUpdatesInBackground(Context context) {
        SharedPreferences defaultSharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE);

        return defaultSharedPreferences.getBoolean(context.getString(R.string.preference_auto_update), true) &&
                Instant.ofEpochMilli(sharedPreferences.getLong(PREFERENCE_KEY_CHECK_INSTANT, 0))
                        .plus(Period.ofDays(1)).isBefore(Instant.now());
    }

    public static void checkedForUpdates(Context context, boolean updateAvailable, String updateVersion) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putLong(PREFERENCE_KEY_CHECK_INSTANT, Instant.now().toEpochMilli());
        editor.putBoolean(PREFERENCE_KEY_UPDATE_AVAILABLE, updateAvailable);
        editor.putString(PREFERENCE_KEY_UPDATE_VERSION, updateVersion);
        editor.apply();
    }

    @NonNull
    public static UpdateInfo getUpdateInfo(Context context) {
        SharedPreferences defaultSharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE);

        return new UpdateInfo(
                sharedPreferences.getString(PREFERENCE_KEY_PREVIOUS_VERSION, null),
                sharedPreferences.getString(PREFERENCE_KEY_NEW_VERSION, null),
                Instant.ofEpochMilli(sharedPreferences.getLong(PREFERENCE_KEY_UPDATE_INSTANT, 0)),
                sharedPreferences.getBoolean(PREFERENCE_KEY_FAILED, false),
                defaultSharedPreferences.getBoolean(context.getString(R.string.preference_auto_update), true),
                Instant.ofEpochMilli(sharedPreferences.getLong(PREFERENCE_KEY_CHECK_INSTANT, 0)),
                sharedPreferences.getBoolean(PREFERENCE_KEY_UPDATE_AVAILABLE, false),
                sharedPreferences.getString(PREFERENCE_KEY_UPDATE_VERSION, null));

    }

    public static boolean isFirstUpdate(Context context) {
        UpdateInfo updateInfo = getUpdateInfo(context);
        return updateInfo.getUpdateInstant().equals(Instant.EPOCH);
    }

}
