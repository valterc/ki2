package com.valterc.ki2.karoo.audio;

import android.annotation.SuppressLint;
import android.util.Log;

import com.valterc.ki2.data.message.AudioAlertEventMessage;
import com.valterc.ki2.data.message.AudioAlertMessage;
import com.valterc.ki2.data.preferences.PreferencesView;
import com.valterc.ki2.karoo.Ki2Context;
import com.valterc.ki2.karoo.hooks.ActivityServiceAudioAlertManagerHook;
import com.valterc.ki2.karoo.hooks.AudioAlertHook;

import java.util.function.Consumer;
import java.util.function.Supplier;

@SuppressWarnings("FieldCanBeLocal")
@SuppressLint("LogNotTimber")
public class LocalAudioAlertManager implements IAudioAlertManager {

    private final Ki2Context context;

    private boolean enableAudioAlerts;
    private String audioAlertLowestGear;
    private String audioAlertHighestGear;
    private String audioAlertShiftingLimit;
    private String audioAlertUpcomingSynchroShift;
    private int delayBetweenAlerts;
    private long timestampLastAlert;

    private final Consumer<PreferencesView> onPreferences = this::onPreferences;
    private final Consumer<AudioAlertMessage> onAudioAlertMessage = this::onAudioAlertMessage;
    private final Consumer<AudioAlertEventMessage> onAudioAlertEventMessage = this::onAudioAlertEventMessage;

    public LocalAudioAlertManager(Ki2Context context) {
        this.context = context;

        context.getServiceClient().registerPreferencesWeakListener(onPreferences);
        context.getServiceClient().getCustomMessageClient().registerAudioAlertWeakListener(onAudioAlertMessage);
        context.getServiceClient().getCustomMessageClient().registerAudioAlertEventWeakListener(onAudioAlertEventMessage);
    }

    private void onPreferences(PreferencesView preferences) {
        enableAudioAlerts = preferences.isAudioAlertsEnabled(context.getSdkContext());
        audioAlertLowestGear = preferences.getAudioAlertLowestGearEnabled(context.getSdkContext());
        audioAlertHighestGear = preferences.getAudioAlertHighestGearEnabled(context.getSdkContext());
        audioAlertShiftingLimit = preferences.getAudioAlertShiftingLimit(context.getSdkContext());
        audioAlertUpcomingSynchroShift = preferences.getAudioAlertUpcomingSynchroShift(context.getSdkContext());
        delayBetweenAlerts = preferences.getDelayBetweenAudioAlerts(context.getSdkContext());
    }

    private void onAudioAlertMessage(AudioAlertMessage audioAlertMessage) {
        Supplier<Boolean> audioAlertCaller = getAudioAlertCaller(audioAlertMessage.getName());
        if (audioAlertCaller == null) {
            Log.w("KI2", "Invalid audio alert name: " + audioAlertMessage.getName());
            return;
        }

        audioAlertCaller.get();
    }

    private void onAudioAlertEventMessage(AudioAlertEventMessage audioAlertEventMessage) {
        switch (audioAlertEventMessage.getEvent()){
            case NONE:
                break;

            case SHIFT_LOWEST_GEAR:
                triggerShiftingLowestGearAudioAlert();
                break;

            case SHIFT_HIGHEST_GEAR:
                triggerShiftingHighestGearAudioAlert();
                break;

            case SHIFT_LIMIT:
                triggerShiftingLimitAudioAlert();
                break;

            case UPCOMING_SYNCHRO_SHIFT:
                triggerSynchroShiftAudioAlert();
                break;

            default:
                Log.w("KI2", "Invalid audio alert event: " + audioAlertEventMessage.getEvent());
        }
    }

    private Supplier<Boolean> getAudioAlertCaller(String audioAlertName) {
        switch (audioAlertName) {
            case "disabled":
                return () -> true;

            case "karoo_workout_interval":
                return () -> ActivityServiceAudioAlertManagerHook.beepKarooWorkoutInterval(context.getSdkContext())
                        || AudioAlertHook.triggerWorkoutNewInterval(context.getSdkContext());

            case "karoo_auto_lap":
                return () -> ActivityServiceAudioAlertManagerHook.beepKarooAutoLap(context.getSdkContext())
                        || AudioAlertHook.triggerAutoLap(context.getSdkContext());

            case "karoo_bell":
                return () -> ActivityServiceAudioAlertManagerHook.beepKarooBell(context.getSdkContext())
                        || AudioAlertHook.triggerBell(context.getSdkContext());

            case "custom_single_beep":
                return () -> ActivityServiceAudioAlertManagerHook.beepSingle(context.getSdkContext())
                        || AudioAlertHook.triggerAutoLap(context.getSdkContext());

            case "custom_double_beep":
                return () -> ActivityServiceAudioAlertManagerHook.beepDouble(context.getSdkContext())
                        || AudioAlertHook.triggerAutoLap(context.getSdkContext());
        }

        return null;
    }

    private void tryTriggerAudioAlert(Runnable audioTrigger) {
        if (!enableAudioAlerts){
            return;
        }
        
        if (System.currentTimeMillis() - timestampLastAlert > delayBetweenAlerts) {
            audioTrigger.run();
            timestampLastAlert = System.currentTimeMillis();
        }
    }

    public void triggerShiftingLowestGearAudioAlert() {
        tryTriggerAudioAlert(() -> {
            Supplier<Boolean> audioAlertCaller = getAudioAlertCaller(audioAlertLowestGear);
            if (audioAlertCaller != null) {
                audioAlertCaller.get();
            }
        });
    }

    public void triggerShiftingHighestGearAudioAlert() {
        tryTriggerAudioAlert(() -> {
            Supplier<Boolean> audioAlertCaller = getAudioAlertCaller(audioAlertHighestGear);
            if (audioAlertCaller != null) {
                audioAlertCaller.get();
            }
        });
    }

    public void triggerShiftingLimitAudioAlert() {
        tryTriggerAudioAlert(() -> {
            Supplier<Boolean> audioAlertCaller = getAudioAlertCaller(audioAlertShiftingLimit);
            if (audioAlertCaller != null) {
                audioAlertCaller.get();
            }
        });
    }

    public void triggerSynchroShiftAudioAlert() {
        tryTriggerAudioAlert(() -> {
            Supplier<Boolean> audioAlertCaller = getAudioAlertCaller(audioAlertUpcomingSynchroShift);
            if (audioAlertCaller != null) {
                audioAlertCaller.get();
            }
        });
    }

}
