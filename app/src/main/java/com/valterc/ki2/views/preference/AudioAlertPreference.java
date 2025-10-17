package com.valterc.ki2.views.preference;

import android.content.ComponentName;
import android.content.Context;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.valterc.ki2.data.message.AudioAlertMessage;
import com.valterc.ki2.services.IKi2Service;
import com.valterc.ki2.services.Ki2Service;

import timber.log.Timber;

@SuppressWarnings("unused")
public class AudioAlertPreference extends SummaryAndValueListPreference {

    private final ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder binder) {
            service = IKi2Service.Stub.asInterface(binder);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            service = null;
        }
    };

    private IKi2Service service;
    private boolean serviceBound;

    public AudioAlertPreference(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public AudioAlertPreference(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public AudioAlertPreference(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public AudioAlertPreference(@NonNull Context context) {
        super(context);
    }

    @Override
    public void setValue(String value) {
        super.setValue(value);

        try {
            if (service != null) {
                service.sendMessage(new AudioAlertMessage(value, true));
            }
        } catch (Exception e) {
            Timber.w(e, "Unable to send message");
        }
    }

    @Override
    public void onAttached() {
        super.onAttached();
        serviceBound = getContext().bindService(Ki2Service.getIntent(), serviceConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    public void onDetached() {
        super.onDetached();

        if (serviceBound) {
            serviceBound = false;
            try {
                getContext().unbindService(serviceConnection);
            } catch (Exception e) {
                // ignore
            }
        }
    }
}
