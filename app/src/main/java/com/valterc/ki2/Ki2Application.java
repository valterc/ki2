package com.valterc.ki2;

import android.app.Activity;
import android.app.Application;
import android.content.ComponentName;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.valterc.ki2.ant.recorder.AntRecorderManager;
import com.valterc.ki2.data.connection.ConnectionInfo;
import com.valterc.ki2.data.device.DeviceId;
import com.valterc.ki2.karoo.Ki2ExtensionService;
import com.valterc.ki2.services.IKi2Service;
import com.valterc.ki2.services.Ki2Service;
import com.valterc.ki2.services.callbacks.IConnectionInfoCallback;

import timber.log.Timber;

public class Ki2Application extends Application {

    private static final int UNREGISTER_FROM_SERVICE_DELAY_MS = 30_000;

    private final ServiceConnection ki2ServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder binder) {
            Timber.d("Service connected");
            service = IKi2Service.Stub.asInterface(binder);
            handler.post(() -> {
                if (activityCount > 0) {
                    registerConnectionInfoListener();
                }
            });
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Timber.d("Service disconnected");
            service = null;
            registeredWithService = false;
        }
    };

    private final ServiceConnection extensionServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder binder) {
            Timber.d("Extension service connected");
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Timber.d("Extension service disconnected");
        }
    };

    private final IConnectionInfoCallback connectionDataInfoCallback = new IConnectionInfoCallback.Stub() {
        @Override
        public void onConnectionInfo(DeviceId deviceId, ConnectionInfo connectionInfo) {
            Timber.d("[%s] Connection status: %s", deviceId, connectionInfo.getConnectionStatus());
        }
    };

    private final ActivityLifecycleCallbacks activityLifecycleCallbacks = new ActivityLifecycleCallbacks() {
        @Override
        public void onActivityCreated(@NonNull Activity activity, @Nullable Bundle savedInstanceState) {
        }

        @Override
        public void onActivityStarted(@NonNull Activity activity) {
            Timber.d("Activity started: %s", activity.getLocalClassName());
            activityCount++;
            handler.post(() -> registerConnectionInfoListener());
        }

        @Override
        public void onActivityResumed(@NonNull Activity activity) {
        }

        @Override
        public void onActivityPaused(@NonNull Activity activity) {
        }

        @Override
        public void onActivityStopped(@NonNull Activity activity) {
            Timber.d("Activity stopped: %s", activity.getLocalClassName());
            activityCount--;
            if (activityCount <= 0) {
                handler.postDelayed(() -> {
                    if (activityCount <= 0) {
                        unregisterConnectionInfoListener();
                    }
                }, UNREGISTER_FROM_SERVICE_DELAY_MS);
            }
        }

        @Override
        public void onActivitySaveInstanceState(@NonNull Activity activity, @NonNull Bundle outState) {
        }

        @Override
        public void onActivityDestroyed(@NonNull Activity activity) {
        }
    };

    private final Handler handler = new Handler();
    private boolean ki2ServiceBound;
    private IKi2Service service;
    private int activityCount;
    private boolean registeredWithService;

    @SuppressWarnings({"FieldCanBeLocal", "unused"})
    private AntRecorderManager antRecorderManager;

    @Override
    public void onCreate() {
        super.onCreate();

        if (BuildConfig.DEBUG) {
            Timber.plant(new Timber.DebugTree() {
                @Override
                protected void log(int priority, @Nullable String tag, @NonNull String message, @Nullable Throwable t) {
                    Log.println(priority, tag, message + (t == null ? "" : "\n" + t.getMessage() + "\n" + Log.getStackTraceString(t)));
                }
            });
        } else {
            Timber.plant(new Timber.Tree() {
                @Override
                protected boolean isLoggable(@Nullable String tag, int priority) {
                    return priority > Log.DEBUG;
                }

                @Override
                protected void log(int priority, @Nullable String tag, @NonNull String message, @Nullable Throwable t) {
                    Log.println(priority, tag, message + (t == null ? "" : "\n" + t.getMessage() + "\n" + Log.getStackTraceString(t)));
                }
            });
        }

        Timber.d("Ki2Application started");

        antRecorderManager = new AntRecorderManager(this);
        registerActivityLifecycleCallbacks(activityLifecycleCallbacks);
        ki2ServiceBound = bindService(Ki2Service.getIntent(), ki2ServiceConnection, BIND_AUTO_CREATE | BIND_IMPORTANT);
        bindService(Ki2ExtensionService.Companion.getIntent(), extensionServiceConnection, BIND_AUTO_CREATE | BIND_IMPORTANT);
    }

    @Override
    public void onTerminate() {
        unregisterConnectionInfoListener();
        if (ki2ServiceBound) {
            unbindService(ki2ServiceConnection);
        }

        Timber.d("Ki2Application terminated");
        super.onTerminate();
    }

    private void registerConnectionInfoListener() {
        if (service == null || registeredWithService) {
            return;
        }

        try {
            service.registerConnectionInfoListener(connectionDataInfoCallback);
            registeredWithService = true;
            Timber.d("Registered with service");
        } catch (RemoteException e) {
            Timber.e(e, "Unable to register connection info listener");
        }
    }

    private void unregisterConnectionInfoListener() {
        if (service == null || !registeredWithService) {
            return;
        }

        try {
            service.unregisterConnectionInfoListener(connectionDataInfoCallback);
            registeredWithService = false;
            Timber.d("Unregistered with service");
        } catch (RemoteException e) {
            Timber.e(e, "Unable to unregister connection info listener");
        }
    }
}
