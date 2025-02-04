package com.valterc.ki2.data.message;

import android.os.Bundle;

public class ToggleOverlayMessage extends Message {
    public static final String KEY = "toggle-overlay";

    public static ToggleOverlayMessage parse(Message message) {
        if (!message.getClassType().equals(ToggleOverlayMessage.class.getName())) {
            return null;
        }

        return new ToggleOverlayMessage(message);
    }

    private ToggleOverlayMessage(Message message) {
        super(message);
    }

    public ToggleOverlayMessage() {
        super(KEY, new Bundle(), MessageType.TOGGLE_OVERLAY, false);
    }

}
