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
import com.valterc.ki2.karoo.Ki2Context;
import com.valterc.ki2.karoo.hooks.RideActivityHook;
import com.valterc.ki2.utils.ActivityUtils;

import java.util.HashMap;
import java.util.function.Consumer;

@SuppressLint("LogNotTimber")
public class VirtualInputAdapter {

    private final HashMap<KarooKey, Consumer<KarooKeyEvent>> keyMapping;

    public VirtualInputAdapter(Ki2Context ki2Context) {
        this.keyMapping = new HashMap<>();
        this.keyMapping.put(KarooKey.VIRTUAL_SWITCH_TO_MAP_PAGE, karooKeyEvent -> {
            boolean result = RideActivityHook.switchToMapPage();
            if (!result) {
                Log.w("KI2", "Unable to switch to map page");
            }
        });
        this.keyMapping.put(KarooKey.VIRTUAL_SHOW_OVERLAY, karooKeyEvent -> ki2Context.getServiceClient().sendMessage(new ShowOverlayMessage()));
        this.keyMapping.put(KarooKey.VIRTUAL_TAKE_SCREENSHOT, karooKeyEvent -> takeScreenshot(ki2Context));
        this.keyMapping.put(KarooKey.VIRTUAL_TURN_SCREEN_ON, karooKeyEvent -> ki2Context.getScreenHelper().turnScreenOn());
        this.keyMapping.put(KarooKey.VIRTUAL_TOGGLE_AUDIO_ALERTS, karooKeyEvent -> toggleAudioAlerts(ki2Context));
        this.keyMapping.put(KarooKey.VIRTUAL_DISABLE_AUDIO_ALERTS, karooKeyEvent -> disableAudioAlerts(ki2Context));
        this.keyMapping.put(KarooKey.VIRTUAL_ENABLE_AUDIO_ALERTS, karooKeyEvent -> enableAudioAlerts(ki2Context));
        this.keyMapping.put(KarooKey.VIRTUAL_SINGLE_BEEP, karooKeyEvent -> ki2Context.getServiceClient().sendMessage(new AudioAlertMessage(ki2Context.getSdkContext().getString(R.string.value_preference_audio_alert_single_beep))));
        this.keyMapping.put(KarooKey.VIRTUAL_DOUBLE_BEEP, karooKeyEvent -> ki2Context.getServiceClient().sendMessage(new AudioAlertMessage(ki2Context.getSdkContext().getString(R.string.value_preference_audio_alert_double_beep))));
    }

    private void showToast(Ki2Context ki2Context, int textResId) {
        Activity activity = ActivityUtils.getRunningActivity();
        if (activity != null) {
            activity.runOnUiThread(() ->
                    Toast.makeText(ki2Context.getSdkContext(), textResId, Toast.LENGTH_SHORT).show());
        }
    }

    private void toggleAudioAlerts(Ki2Context ki2Context) {
        PreferencesView preferences = ki2Context.getServiceClient().getPreferences();
        ki2Context.getServiceClient().sendMessage(new AudioAlertToggleMessage());
        if (preferences != null) {
            showToast(ki2Context, preferences.isAudioAlertsEnabled(ki2Context.getSdkContext()) ? R.string.text_disabled_audio_alerts : R.string.text_enabled_audio_alerts);
        }
    }

    private void disableAudioAlerts(Ki2Context ki2Context) {
        ki2Context.getServiceClient().sendMessage(new AudioAlertDisableMessage());
        showToast(ki2Context, R.string.text_disabled_audio_alerts);
    }

    private void enableAudioAlerts(Ki2Context ki2Context) {
        ki2Context.getServiceClient().sendMessage(new AudioAlertEnableMessage());
        showToast(ki2Context, R.string.text_enabled_audio_alerts);
    }

    private void takeScreenshot(Ki2Context ki2Context) {
        boolean result = ki2Context.getScreenHelper().takeScreenshot();
        if (result) {
            showToast(ki2Context, R.string.text_screenshot_saved);
        }
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
