package com.valterc.ki2.data.update;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.Nullable;

import java.time.Instant;

public final class UpdateStateStore {

    private static final String PREFERENCE_NAME = "UpdateState";
    private static final String PREFERENCE_KEY_UPDATED = "Updated";
    private static final String PREFERENCE_KEY_PREVIOUS_VERSION = "PreviousVersion";
    private static final String PREFERENCE_KEY_NEW_VERSION = "NewVersion";
    private static final String PREFERENCE_UPDATE_INSTANT = "Instant";

    private UpdateStateStore() {
    }

    @SuppressLint("ApplySharedPref")
    public static void willUpdate(Context context, String previousVersion, String newVersion) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(PREFERENCE_KEY_PREVIOUS_VERSION, previousVersion);
        editor.putString(PREFERENCE_KEY_NEW_VERSION, newVersion);
        editor.putLong(PREFERENCE_UPDATE_INSTANT, Instant.now().toEpochMilli());
        editor.putBoolean(PREFERENCE_KEY_UPDATED, true);
        editor.commit();
    }

    public static void updateFailed(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();
    }

    @SuppressLint("ApplySharedPref")
    @Nullable
    public static UpdateStateInfo getAndClearUpdateState(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE);
        if (!sharedPreferences.getBoolean(PREFERENCE_KEY_UPDATED, false)) {
            return null;
        }

        UpdateStateInfo updateStateInfo = new UpdateStateInfo(
                sharedPreferences.getString(PREFERENCE_KEY_PREVIOUS_VERSION, null),
                sharedPreferences.getString(PREFERENCE_KEY_NEW_VERSION, null),
                Instant.ofEpochMilli(sharedPreferences.getLong(PREFERENCE_UPDATE_INSTANT, Instant.now().toEpochMilli()))
        );

        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.commit();

        return updateStateInfo;
    }

    public static boolean isUpdateOngoing(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getBoolean(PREFERENCE_KEY_UPDATED, false);
    }

}
