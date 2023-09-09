package com.valterc.ki2.data.message;

import android.os.Bundle;

public class AudioAlertToggleMessage extends Message {
    public static final String KEY = "audio-alert-toggle";

    public static AudioAlertToggleMessage parse(Message message) {
        if (!message.getClassType().equals(AudioAlertToggleMessage.class.getName())) {
            return null;
        }

        return new AudioAlertToggleMessage(message);
    }

    private AudioAlertToggleMessage(Message message) {
        super(message);
    }

    public AudioAlertToggleMessage() {
        super(KEY, new Bundle(), MessageType.AUDIO_ALERT_TOGGLE, false);
    }

}
