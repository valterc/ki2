package com.valterc.ki2.data.message;

import android.os.Bundle;

public class AudioAlertMessage extends Message {
    private static final String KEY_NAME = "name";
    public static final String KEY = "audio-alert";

    public static AudioAlertMessage parse(Message message) {
        if (!message.getClassType().equals(AudioAlertMessage.class.getName())) {
            return null;
        }

        return new AudioAlertMessage(message);
    }

    private final String name;

    private AudioAlertMessage(Message message) {
        super(message);
        name = getBundle().getString(KEY_NAME);
    }

    public AudioAlertMessage(String name) {
        super(KEY, new Bundle(), MessageType.AUDIO_ALERT, false);

        getBundle().putString(KEY_NAME, name);
        this.name = name;
    }

    public String getName() {
        return name;
    }

}
