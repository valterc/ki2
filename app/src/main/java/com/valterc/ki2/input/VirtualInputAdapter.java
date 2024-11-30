package com.valterc.ki2.input;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.util.Log;
import android.widget.Toast;

import com.valterc.ki2.R;
import com.valterc.ki2.data.input.KarooKey;
import com.valterc.ki2.data.input.KarooKeyEvent;
import com.valterc.ki2.data.message.AudioAlertDisableMessage;
import com.valterc.ki2.data.message.AudioAlertEnableMessage;
import com.valterc.ki2.data.message.AudioAlertMessage;
import com.valterc.ki2.data.message.AudioAlertToggleMessage;
import com.valterc.ki2.data.message.ShowOverlayMessage;
import com.valterc.ki2.data.preferences.PreferencesView;
import com.valterc.ki2.karoo.extension.Ki2ExtensionContext;
import com.valterc.ki2.karoo.hooks.RideActivityHook;
import com.valterc.ki2.utils.ActivityUtils;

import java.util.HashMap;
import java.util.function.Consumer;

import io.hammerhead.karooext.models.TurnScreenOn;

@SuppressLint("LogNotTimber")
public class VirtualInputAdapter {

    private final HashMap<KarooKey, Consumer<KarooKeyEvent>> keyMapping;

    public VirtualInputAdapter(Ki2ExtensionContext context) {
        this.keyMapping = new HashMap<>();
        this.keyMapping.put(KarooKey.VIRTUAL_SWITCH_TO_MAP_PAGE, karooKeyEvent -> {
            boolean result = RideActivityHook.switchToMapPage();
            if (!result) {
                Log.w("KI2", "Unable to switch to map page");
            }
        });
        this.keyMapping.put(KarooKey.VIRTUAL_SHOW_OVERLAY, karooKeyEvent -> context.getServiceClient().sendMessage(new ShowOverlayMessage()));
        this.keyMapping.put(KarooKey.VIRTUAL_TURN_SCREEN_ON, karooKeyEvent -> context.getKarooSystem().dispatch(TurnScreenOn.INSTANCE));
        this.keyMapping.put(KarooKey.VIRTUAL_TOGGLE_AUDIO_ALERTS, karooKeyEvent -> toggleAudioAlerts(context));
        this.keyMapping.put(KarooKey.VIRTUAL_DISABLE_AUDIO_ALERTS, karooKeyEvent -> disableAudioAlerts(context));
        this.keyMapping.put(KarooKey.VIRTUAL_ENABLE_AUDIO_ALERTS, karooKeyEvent -> enableAudioAlerts(context));
        this.keyMapping.put(KarooKey.VIRTUAL_SINGLE_BEEP, karooKeyEvent -> context.getServiceClient().sendMessage(new AudioAlertMessage(context.getContext().getString(R.string.value_preference_audio_alert_single_beep))));
        this.keyMapping.put(KarooKey.VIRTUAL_DOUBLE_BEEP, karooKeyEvent -> context.getServiceClient().sendMessage(new AudioAlertMessage(context.getContext().getString(R.string.value_preference_audio_alert_double_beep))));
        this.keyMapping.put(KarooKey.VIRTUAL_BELL, karooKeyEvent -> context.getServiceClient().sendMessage(new AudioAlertMessage(context.getContext().getString(R.string.value_preference_audio_alert_bell))));
    }

    private void showToast(Ki2ExtensionContext context, int textResId) {
        // TODO: show in ride message
    }

    private void toggleAudioAlerts(Ki2ExtensionContext context) {
        PreferencesView preferences = context.getServiceClient().getPreferences();
        context.getServiceClient().sendMessage(new AudioAlertToggleMessage());
        if (preferences != null) {
            showToast(context, preferences.isAudioAlertsEnabled(context.getContext()) ? R.string.text_disabled_audio_alerts : R.string.text_enabled_audio_alerts);
        }
    }

    private void disableAudioAlerts(Ki2ExtensionContext context) {
        context.getServiceClient().sendMessage(new AudioAlertDisableMessage());
        showToast(context, R.string.text_disabled_audio_alerts);
    }

    private void enableAudioAlerts(Ki2ExtensionContext context) {
        context.getServiceClient().sendMessage(new AudioAlertEnableMessage());
        showToast(context, R.string.text_enabled_audio_alerts);
    }

    public void handleVirtualKeyEvent(KarooKeyEvent keyEvent) {
        if (!keyEvent.getKey().isVirtual()) {
            return;
        }

        Consumer<KarooKeyEvent> keyEventConsumer = keyMapping.get(keyEvent.getKey());
        if (keyEventConsumer != null) {
            keyEventConsumer.accept(keyEvent);
        }
    }
}
