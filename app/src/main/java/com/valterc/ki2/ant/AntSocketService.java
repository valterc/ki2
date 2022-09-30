package com.valterc.ki2.ant;

import android.app.ActivityManager;
import android.content.Context;

import java.lang.reflect.Method;

public final class AntSocketService {

    private static final String ANT_SOCKET_SERVICE = "com.dsi.ant.service.socket";

    private static void invokeForceStopPackageMethod(ActivityManager activityManager, String packageName) throws Exception {
        Method method = ActivityManager.class.getMethod("forceStopPackage", String.class);
        method.invoke(activityManager, packageName);
    }

    /**
     * Forces the Ant Socket service to be stopped.
     *
     * @param context Context of the current caller.
     * @throws Exception Exception if some error occurs during the force stop.
     */
    public static void ForceStop(Context context) throws Exception {
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        invokeForceStopPackageMethod(activityManager, ANT_SOCKET_SERVICE);
    }

    private AntSocketService() {
    }

}
