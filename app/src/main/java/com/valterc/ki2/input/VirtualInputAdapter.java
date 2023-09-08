package com.valterc.ki2.input;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.util.Log;
import android.widget.Toast;

import com.valterc.ki2.R;
import com.valterc.ki2.data.input.KarooKey;
import com.valterc.ki2.data.input.KarooKeyEvent;
import com.valterc.ki2.data.message.ShowOverlayMessage;
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
    }

    private void takeScreenshot(Ki2Context ki2Context) {
        boolean result = ki2Context.getScreenHelper().takeScreenshot();
        if (result) {
            Activity activity = ActivityUtils.getRunningActivity();
            if (activity != null) {
                activity.runOnUiThread(() ->
                        Toast.makeText(ki2Context.getSdkContext(), R.string.text_screenshot_saved, Toast.LENGTH_SHORT).show());
            }
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
