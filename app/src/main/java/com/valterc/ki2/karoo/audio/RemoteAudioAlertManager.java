package com.valterc.ki2.karoo.audio;

import com.valterc.ki2.data.audio.AudioAlertEvent;
import com.valterc.ki2.data.message.AudioAlertEventMessage;
import com.valterc.ki2.karoo.Ki2Context;

public class RemoteAudioAlertManager implements IAudioAlertManager {

    private final Ki2Context context;

    public RemoteAudioAlertManager(Ki2Context context) {
        this.context = context;
    }

    @Override
    public void triggerShiftingLowestGearAudioAlert() {
        context.getServiceClient().sendMessage(new AudioAlertEventMessage(AudioAlertEvent.SHIFT_LOWEST_GEAR));
    }

    @Override
    public void triggerShiftingHighestGearAudioAlert() {
        context.getServiceClient().sendMessage(new AudioAlertEventMessage(AudioAlertEvent.SHIFT_HIGHEST_GEAR));
    }

    @Override
    public void triggerShiftingLimitAudioAlert() {
        context.getServiceClient().sendMessage(new AudioAlertEventMessage(AudioAlertEvent.SHIFT_LIMIT));
    }

    @Override
    public void triggerSynchroShiftAudioAlert() {
        context.getServiceClient().sendMessage(new AudioAlertEventMessage(AudioAlertEvent.UPCOMING_SYNCHRO_SHIFT));
    }
}
