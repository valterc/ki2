package com.valterc.ki2.karoo.audio;

public interface IAudioAlertManager {

    void triggerShiftingLowestGearAudioAlert();

    void triggerShiftingHighestGearAudioAlert();

    void triggerShiftingLimitAudioAlert();

    void triggerSynchroShiftAudioAlert();

}
