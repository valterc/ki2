package com.valterc.ki2.data.message;

import android.os.Bundle;

public class AudioAlertDisableMessage extends Message {
    public static final String KEY = "audio-alert-disable";

    public static AudioAlertDisableMessage parse(Message message) {
        if (!message.getClassType().equals(AudioAlertDisableMessage.class.getName())) {
            return null;
        }

        return new AudioAlertDisableMessage(message);
    }

    private AudioAlertDisableMessage(Message message) {
        super(message);
    }

    public AudioAlertDisableMessage() {
        super(KEY, new Bundle(), MessageType.AUDIO_ALERT_DISABLE, false);
    }

}
