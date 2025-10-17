package com.valterc.ki2.data.message;

import android.os.Bundle;

public class AudioAlertMessage extends Message {
    public static final String KEY = "audio-alert";
    private static final String KEY_NAME = "name";
    private static final String KEY_ADJUST_INTENSITY = "adjust-intensity";

    public static AudioAlertMessage parse(Message message) {
        if (!message.getClassType().equals(AudioAlertMessage.class.getName())) {
            return null;
        }

        return new AudioAlertMessage(message);
    }

    private final String name;
    private final boolean adjustIntensity;

    private AudioAlertMessage(Message message) {
        super(message);
        name = getBundle().getString(KEY_NAME);
        adjustIntensity = getBundle().getBoolean(KEY_ADJUST_INTENSITY);
    }

    public AudioAlertMessage(String name, boolean adjustIntensity) {
        super(KEY, new Bundle(), MessageType.AUDIO_ALERT, false);

        getBundle().putString(KEY_NAME, name);
        getBundle().putBoolean(KEY_ADJUST_INTENSITY, adjustIntensity);
        this.name = name;
        this.adjustIntensity = adjustIntensity;
    }

    public AudioAlertMessage(String name) {
        this(name, true);
    }

    public String getName() {
        return name;
    }

    public boolean getAdjustIntensity() {
        return adjustIntensity;
    }
}
