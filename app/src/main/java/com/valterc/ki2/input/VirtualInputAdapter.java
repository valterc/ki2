package com.valterc.ki2.input;

import android.annotation.SuppressLint;
import android.util.Log;

import com.valterc.ki2.data.input.KarooKey;
import com.valterc.ki2.data.input.KarooKeyEvent;
import com.valterc.ki2.data.message.ShowOverlayMessage;
import com.valterc.ki2.karoo.Ki2Context;
import com.valterc.ki2.karoo.hooks.RideActivityHook;

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
        this.keyMapping.put(KarooKey.VIRTUAL_TURN_SCREEN_ON, karooKeyEvent -> ki2Context.getScreenHelper().switchTurnScreenOn());
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
