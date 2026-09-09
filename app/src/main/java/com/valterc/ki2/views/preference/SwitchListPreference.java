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
import com.valterc.ki2.external.ExternalAction;
import com.valterc.ki2.external.ExternalActionDescriptor;
import com.valterc.ki2.external.ExternalActionManager;
import com.valterc.ki2.services.IKi2Service;
import com.valterc.ki2.services.Ki2Service;

import java.util.ArrayList;
import java.util.List;

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
    private CharSequence[] baseEntries;
    private CharSequence[] baseEntryValues;
    private ExternalActionManager externalActionManager;
    private final ExternalActionManager.Listener actionsListener = this::rebuildEntries;

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
        externalActionManager = ExternalActionManager.getInstance(getContext());
        externalActionManager.addListener(actionsListener);
        rebuildEntries();
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

        if (externalActionManager != null) {
            externalActionManager.removeListener(actionsListener);
        }
    }

    private void rebuildEntries() {
        if (baseEntries == null) {
            baseEntries = getEntries();
            baseEntryValues = getEntryValues();
        }

        if (baseEntries == null || baseEntryValues == null) {
            return;
        }

        if (externalActionManager == null) {
            externalActionManager = ExternalActionManager.getInstance(getContext());
        }

        int switchMask = getSwitchMaskForPreferenceKey(getKey());
        List<ExternalActionDescriptor> externalActions = externalActionManager.getExternalActionsSnapshot();

        List<CharSequence> entries = new ArrayList<>();
        List<CharSequence> values = new ArrayList<>();

        for (int i = 0; i < baseEntries.length; i++) {
            entries.add(baseEntries[i]);
            values.add(baseEntryValues[i]);
        }

        for (ExternalActionDescriptor descriptor : externalActions) {
            ExternalAction action = descriptor.getAction();
            if (!isAllowedForSwitch(action, switchMask)) {
                continue;
            }
            entries.add(action.getLabel() + " (" + descriptor.getAppLabel() + ")");
            values.add(ExternalActionManager.toPreferenceValue(descriptor.getProviderComponent(), action.getActionId()));
        }

        setEntries(entries.toArray(new CharSequence[0]));
        setEntryValues(values.toArray(new CharSequence[0]));
    }

    private boolean isAllowedForSwitch(@NonNull ExternalAction action, int switchMask) {
        if (switchMask == 0) {
            return true;
        }
        return (action.getAllowedSwitches() & switchMask) != 0;
    }

    private int getSwitchMaskForPreferenceKey(@Nullable String key) {
        if (key == null) {
            return 0;
        }
        if (key.contains("CH1")) {
            return ExternalAction.SWITCH_CH1;
        }
        if (key.contains("CH2")) {
            return ExternalAction.SWITCH_CH2;
        }
        if (key.contains("CH3")) {
            return ExternalAction.SWITCH_CH3;
        }
        if (key.contains("CH4")) {
            return ExternalAction.SWITCH_CH4;
        }
        return 0;
    }
}
