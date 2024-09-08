package com.valterc.ki2.ant;

import android.content.Context;
import android.net.Uri;
import android.provider.Settings;

public final class AntSettings {

    private AntSettings() {
    }

    public static final String ENABLED = "ant_plus_enabled";

    public static Uri getEnabledUri() {
        return Settings.Global.getUriFor(ENABLED);
    }

    public static boolean isAntEnabled(Context context) {
        return Settings.Global.getInt(context.getContentResolver(), ENABLED, 0) == 1;
    }

    public static void enableAnt(Context context) {
        Settings.Global.putInt(context.getContentResolver(), ENABLED, 1);
    }

}
