package com.valterc.ki2.input;

import android.annotation.SuppressLint;

import com.valterc.ki2.data.action.KarooActionEvent;
import com.valterc.ki2.data.device.DeviceId;
import com.valterc.ki2.data.preferences.PreferencesView;
import com.valterc.ki2.karoo.Ki2ExtensionContext;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

import io.hammerhead.karooext.models.TurnScreenOn;

@SuppressLint("LogNotTimber")
@SuppressWarnings("FieldCanBeLocal")
public class ActionManager {

    private final Ki2ExtensionContext context;
    private final ActionAdapter actionAdapter;
    private boolean switchesTurnScreenOn;

    private final Consumer<PreferencesView> onPreferences = this::onPreferences;
    private final BiConsumer<DeviceId, KarooActionEvent> onAction = this::onAction;

    public ActionManager(Ki2ExtensionContext context) {
        this.context = context;
        this.actionAdapter = new ActionAdapter(context);

        context.getServiceClient().registerPreferencesWeakListener(onPreferences);
        context.getServiceClient().registerActionEventWeakListener(onAction);
    }

    private void onPreferences(PreferencesView preferencesView) {
        switchesTurnScreenOn = preferencesView.isSwitchTurnScreenOn(context.getContext());
    }

    private void onAction(DeviceId deviceId, KarooActionEvent karooActionEvent) {
        if (switchesTurnScreenOn) {
            context.getKarooSystem().dispatch(TurnScreenOn.INSTANCE);
        }

        for (int i = 0; i < karooActionEvent.getReplicate(); i++) {
            actionAdapter.handleVirtualKeyEvent(karooActionEvent);
        }
    }
}
