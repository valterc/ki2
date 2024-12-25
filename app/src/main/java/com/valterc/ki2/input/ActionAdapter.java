package com.valterc.ki2.input;

import android.annotation.SuppressLint;

import com.valterc.ki2.R;
import com.valterc.ki2.data.action.KarooAction;
import com.valterc.ki2.data.action.KarooActionEvent;
import com.valterc.ki2.data.message.AudioAlertDisableMessage;
import com.valterc.ki2.data.message.AudioAlertEnableMessage;
import com.valterc.ki2.data.message.AudioAlertToggleMessage;
import com.valterc.ki2.data.message.ShowOverlayMessage;
import com.valterc.ki2.data.preferences.PreferencesView;
import com.valterc.ki2.karoo.extension.Ki2ExtensionContext;

import java.util.HashMap;
import java.util.function.Consumer;

import io.hammerhead.karooext.models.InRideAlert;
import io.hammerhead.karooext.models.MarkLap;
import io.hammerhead.karooext.models.PerformHardwareAction;
import io.hammerhead.karooext.models.ShowMapPage;
import io.hammerhead.karooext.models.TurnScreenOff;
import io.hammerhead.karooext.models.TurnScreenOn;
import io.hammerhead.karooext.models.ZoomPage;

@SuppressLint("LogNotTimber")
public class ActionAdapter {

    private final HashMap<KarooAction, Consumer<KarooActionEvent>> keyMapping;

    public ActionAdapter(Ki2ExtensionContext context) {
        this.keyMapping = new HashMap<>();

        this.keyMapping.put(KarooAction.TOP_LEFT, karooKeyEvent -> context.getKarooSystem().dispatch(PerformHardwareAction.TopLeftPress.INSTANCE));
        this.keyMapping.put(KarooAction.BOTTOM_LEFT, karooKeyEvent -> context.getKarooSystem().dispatch(PerformHardwareAction.BottomLeftPress.INSTANCE));
        this.keyMapping.put(KarooAction.TOP_RIGHT, karooKeyEvent -> context.getKarooSystem().dispatch(PerformHardwareAction.TopRightPress.INSTANCE));
        this.keyMapping.put(KarooAction.BOTTOM_RIGHT, karooKeyEvent -> context.getKarooSystem().dispatch(PerformHardwareAction.BottomRightPress.INSTANCE));

        this.keyMapping.put(KarooAction.VIRTUAL_SHOW_OVERLAY, karooKeyEvent -> context.getServiceClient().sendMessage(new ShowOverlayMessage()));
        this.keyMapping.put(KarooAction.VIRTUAL_TURN_SCREEN_ON, karooKeyEvent -> context.getKarooSystem().dispatch(TurnScreenOn.INSTANCE));
        this.keyMapping.put(KarooAction.VIRTUAL_TURN_SCREEN_OFF, karooKeyEvent -> context.getKarooSystem().dispatch(TurnScreenOff.INSTANCE));
        this.keyMapping.put(KarooAction.VIRTUAL_SWITCH_TO_MAP_PAGE, karooKeyEvent -> context.getKarooSystem().dispatch(new ShowMapPage()));
        this.keyMapping.put(KarooAction.VIRTUAL_TOGGLE_AUDIO_ALERTS, karooKeyEvent -> toggleAudioAlerts(context));
        this.keyMapping.put(KarooAction.VIRTUAL_DISABLE_AUDIO_ALERTS, karooKeyEvent -> disableAudioAlerts(context));
        this.keyMapping.put(KarooAction.VIRTUAL_ENABLE_AUDIO_ALERTS, karooKeyEvent -> enableAudioAlerts(context));
        this.keyMapping.put(KarooAction.VIRTUAL_SINGLE_BEEP, karooKeyEvent -> context.getAudioManager().playSingleBeep());
        this.keyMapping.put(KarooAction.VIRTUAL_DOUBLE_BEEP, karooKeyEvent -> context.getAudioManager().playDoubleBeep());
        this.keyMapping.put(KarooAction.VIRTUAL_BELL, karooKeyEvent -> context.getAudioManager().playKarooBell());
        this.keyMapping.put(KarooAction.VIRTUAL_LAP, karooKeyEvent -> context.getKarooSystem().dispatch(MarkLap.INSTANCE));
        this.keyMapping.put(KarooAction.VIRTUAL_ZOOM_IN, karooKeyEvent -> context.getKarooSystem().dispatch(new ZoomPage(true)));
        this.keyMapping.put(KarooAction.VIRTUAL_ZOOM_OUT, karooKeyEvent -> context.getKarooSystem().dispatch(new ZoomPage(false)));
        this.keyMapping.put(KarooAction.VIRTUAL_CONTROL_CENTER, karooKeyEvent -> context.getKarooSystem().dispatch(PerformHardwareAction.ControlCenterComboPress.INSTANCE));
    }

    private void showToast(Ki2ExtensionContext context, int textResId) {
        context.getKarooSystem()
                .dispatch(new InRideAlert("input", R.drawable.ic_audio_alert, context.getContext().getString(R.string.category_preference_audio_alerts), context.getContext().getString(textResId), 3000L, R.color.grey_100, R.color.black));
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

    public void handleVirtualKeyEvent(KarooActionEvent keyEvent) {
        Consumer<KarooActionEvent> keyEventConsumer = keyMapping.get(keyEvent.getAction());
        if (keyEventConsumer != null) {
            keyEventConsumer.accept(keyEvent);
        }
    }
}
