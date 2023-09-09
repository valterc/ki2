package com.valterc.ki2.data.message;

import android.os.Bundle;

import com.valterc.ki2.data.audio.AudioAlertEvent;

public class AudioAlertEnableMessage extends Message {
    public static final String KEY = "audio-alert-enable";

    public static AudioAlertEnableMessage parse(Message message) {
        if (!message.getClassType().equals(AudioAlertEnableMessage.class.getName())) {
            return null;
        }

        return new AudioAlertEnableMessage(message);
    }

    private AudioAlertEnableMessage(Message message) {
        super(message);
    }

    public AudioAlertEnableMessage() {
        super(KEY, new Bundle(), MessageType.AUDIO_ALERT_ENABLE, false);
    }

}
