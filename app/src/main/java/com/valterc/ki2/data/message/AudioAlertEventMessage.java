package com.valterc.ki2.data.message;

import android.os.Bundle;

import com.valterc.ki2.data.audio.AudioAlertEvent;

public class AudioAlertEventMessage extends Message {
    private static final String KEY_EVENT = "event";
    public static final String KEY = "audio-alert-event";

    public static AudioAlertEventMessage parse(Message message) {
        if (!message.getClassType().equals(AudioAlertEventMessage.class.getName())) {
            return null;
        }

        return new AudioAlertEventMessage(message);
    }

    private final AudioAlertEvent event;

    private AudioAlertEventMessage(Message message) {
        super(message);
        event = AudioAlertEvent.fromOrdinal( getBundle().getInt(KEY_EVENT));
    }

    public AudioAlertEventMessage(AudioAlertEvent event) {
        super(KEY, new Bundle(), MessageType.AUDIO_ALERT_EVENT, false);

        getBundle().putInt(KEY_EVENT, event.ordinal());
        this.event = event;
    }

    public AudioAlertEvent getEvent() {
        return event;
    }

}
