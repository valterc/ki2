package com.valterc.ki2.views.preference;

import android.content.ComponentName;
import android.content.Context;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.preference.ListPreference;

import com.valterc.ki2.data.message.AudioAlertMessage;
import com.valterc.ki2.services.IKi2Service;
import com.valterc.ki2.services.Ki2Service;

import timber.log.Timber;

@SuppressWarnings("unused")
public class SwitchListPreference extends ListPreference {

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

    public SwitchListPreference(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public SwitchListPreference(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public SwitchListPreference(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public SwitchListPreference(@NonNull Context context) {
        super(context);
    }

    @Override
    public void setValue(String value) {
        super.setValue(value);

        try {
            if (service != null) {
                switch (value) {
                    case "press_single_beep":
                    case "hold_short_single_single_beep":
                    case "hold_continuous_single_beep":
                        service.sendMessage(new AudioAlertMessage("custom_single_beep", false));
                        break;
                    case "press_double_beep":
                    case "hold_short_single_double_beep":
                        service.sendMessage(new AudioAlertMessage("custom_double_beep", false));
                        break;
                    case "press_bell_old":
                    case "hold_short_single_bell_old":
                    case "hold_continuous_bell_old":
                        service.sendMessage(new AudioAlertMessage("karoo_bell_old", false));
                        break;
                    case "press_bell_new" :
                    case "hold_short_single_bell_new":
                    case "hold_continuous_bell_new":
                        service.sendMessage(new AudioAlertMessage("karoo_bell_new", false));
                        break;
                }
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