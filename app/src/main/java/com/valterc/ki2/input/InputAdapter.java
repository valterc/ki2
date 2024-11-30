package com.valterc.ki2.input;

import android.annotation.SuppressLint;
import android.os.SystemClock;
import android.view.ViewConfiguration;

import com.valterc.ki2.data.input.KarooKeyEvent;
import com.valterc.ki2.data.input.KarooKey;
import com.valterc.ki2.data.preferences.PreferencesView;
import com.valterc.ki2.karoo.extension.Ki2ExtensionContext;

import java.util.HashMap;
import java.util.function.Consumer;

import io.hammerhead.karooext.models.TurnScreenOn;

@SuppressLint("LogNotTimber")
@SuppressWarnings("FieldCanBeLocal")
public class InputAdapter {

    private final Ki2ExtensionContext context;
    private final VirtualInputAdapter virtualInputAdapter;
    private boolean switchesTurnScreenOn;

    private final Consumer<PreferencesView> onPreferences = this::onPreferences;

    public InputAdapter(Ki2ExtensionContext context) {
        this.context = context;
        this.virtualInputAdapter = new VirtualInputAdapter(context);
    }

    private void onPreferences(PreferencesView preferencesView) {
        switchesTurnScreenOn = preferencesView.isSwitchTurnScreenOn(context.getContext());
    }

    public void executeKeyEvent(KarooKeyEvent keyEvent) {
        if (switchesTurnScreenOn) {
            context.getKarooSystem().dispatch(TurnScreenOn.INSTANCE);
        }

        if (keyEvent.getKey().isVirtual()) {
            virtualInputAdapter.handleVirtualKeyEvent(keyEvent);
        } else {
            for (int i = 0; i < keyEvent.getReplicate(); i++) {
                long eventTime = SystemClock.uptimeMillis() + (long) ViewConfiguration.getKeyRepeatTimeout() * i;
                switch (keyEvent.getAction()) {
                    case SINGLE_PRESS:
                        //keyPressed(keyEvent.getKey(), eventTime);
                        break;

                    case DOUBLE_PRESS:
                        //keyPressed(keyEvent.getKey(), eventTime);
                        //keyPressed(keyEvent.getKey(), eventTime);
                        break;

                    case LONG_PRESS_DOWN:
                    case LONG_PRESS_CONTINUE:
                        //setKeyDown(keyEvent.getKey(), keyEvent.getRepeat());
                        break;

                    case LONG_PRESS_UP:
                        //setKeyUp(keyEvent.getKey());
                        break;

                    case SIMULATE_LONG_PRESS:
                        //simulateLongKeyPress(keyEvent.getKey(), eventTime);
                        break;
                }
            }
        }
    }

}
