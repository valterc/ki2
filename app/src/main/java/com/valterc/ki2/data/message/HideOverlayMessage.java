package com.valterc.ki2.data.message;

import android.os.Bundle;

public class HideOverlayMessage extends Message {
    public static final String KEY = "hide-overlay";

    public static HideOverlayMessage parse(Message message) {
        if (!message.getClassType().equals(HideOverlayMessage.class.getName())) {
            return null;
        }

        return new HideOverlayMessage(message);
    }

    private HideOverlayMessage(Message message) {
        super(message);
    }

    public HideOverlayMessage() {
        super(KEY, new Bundle(), MessageType.HIDE_OVERLAY, false);
    }

}
