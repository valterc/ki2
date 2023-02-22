package com.valterc.ki2.data.message;

import android.os.Bundle;

public class ShowOverlayMessage extends Message {
    public static final String KEY = "show-overlay";

    public static ShowOverlayMessage parse(Message message) {
        if (!message.getClassType().equals(ShowOverlayMessage.class.getName())) {
            return null;
        }

        return new ShowOverlayMessage(message);
    }

    private ShowOverlayMessage(Message message) {
        super(message);
    }

    public ShowOverlayMessage() {
        super(KEY, new Bundle(), MessageType.SHOW_OVERLAY, false);
    }

}
