package com.valterc.ki2.input;

import android.annotation.SuppressLint;
import android.util.Log;

import com.valterc.ki2.data.input.KarooKey;
import com.valterc.ki2.data.input.KarooKeyEvent;
import com.valterc.ki2.karoo.hooks.RideActivityHook;

import java.util.HashMap;
import java.util.function.Consumer;

@SuppressLint("LogNotTimber")
public class VirtualInputAdapter {

    private final HashMap<KarooKey, Consumer<KarooKeyEvent>> keyMapping;

    public VirtualInputAdapter() {
        this.keyMapping = new HashMap<>();
        this.keyMapping.put(KarooKey.VIRTUAL_SWITCH_TO_MAP_PAGE, karooKeyEvent -> {
            boolean result = RideActivityHook.switchToMapPage();
            if (!result) {
                Log.w("KI2", "Unable to switch to map page");
            }
        });
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
