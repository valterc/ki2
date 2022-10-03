package com.valterc.ki2;

import android.app.Application;
import android.content.ComponentName;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.valterc.ki2.data.connection.ConnectionDataInfo;
import com.valterc.ki2.data.connection.ConnectionInfo;
import com.valterc.ki2.data.device.DeviceId;
import com.valterc.ki2.karoo.Ki2BroadcastReceiver;
import com.valterc.ki2.services.IKi2Service;
import com.valterc.ki2.services.Ki2Service;
import com.valterc.ki2.services.callbacks.IConnectionDataInfoCallback;
import com.valterc.ki2.services.callbacks.IConnectionInfoCallback;

import io.hammerhead.sdk.v0.SdkContext;
import timber.log.Timber;

public class Ki2Application extends Application {

    private final ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder binder) {
            Timber.d("Service connected");
            service = IKi2Service.Stub.asInterface(binder);

            try {
                service.registerConnectionInfoListener(connectionDataInfoCallback);
            } catch (RemoteException e) {
                Timber.e(e, "Unable to register connection info listener");
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Timber.d("Service disconnected");
            service = null;
        }
    };

    private final IConnectionInfoCallback connectionDataInfoCallback = new IConnectionInfoCallback.Stub() {
        @Override
        public void onConnectionInfo(DeviceId deviceId, ConnectionInfo connectionInfo) throws RemoteException {
            Timber.d("[%s] Connection status: %s", deviceId, connectionInfo.getStatus());
        }
    };

    private boolean serviceBound;
    private IKi2Service service;

    @Override
    public void onCreate() {
        super.onCreate();

        serviceBound = bindService(Ki2Service.getIntent(), serviceConnection, BIND_AUTO_CREATE);

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
    }

    @Override
    public void onTerminate() {
        if (serviceBound){
            unbindService(serviceConnection);
        }

        if (service != null) {
            try {
                service.unregisterConnectionInfoListener(connectionDataInfoCallback);
            } catch (RemoteException e) {
                Timber.e(e, "Unable to unregister connection info listener");
            }
        }
        super.onTerminate();
    }
}
