package com.valterc.ki2.karoo.gears;

import com.valterc.ki2.data.device.DeviceId;
import com.valterc.ki2.data.preferences.PreferencesView;
import com.valterc.ki2.data.shifting.BuzzerType;
import com.valterc.ki2.data.shifting.ShiftingInfo;
import com.valterc.ki2.karoo.Ki2Context;
import com.valterc.ki2.karoo.handlers.IRideHandler;
import com.valterc.ki2.karoo.hooks.AudioAlertHook;

import java.util.HashMap;
import java.util.Map;

public class GearsAudioAlertHandler implements IRideHandler {

    private final Ki2Context context;
    private final Map<DeviceId, ShiftingInfo> deviceShiftingMap;
    private boolean alertEnabledLowestGear;
    private boolean alertEnabledHighestGear;
    private boolean alertEnabledShiftingLimit;
    private boolean alertEnabledUpcomingSynchroShift;
    private boolean riding;

    public GearsAudioAlertHandler(Ki2Context context) {
        this.context = context;
        this.deviceShiftingMap = new HashMap<>();

        PreferencesView preferences = context.getServiceClient().getPreferences();
        alertEnabledLowestGear = preferences.isAudioAlertLowestGearEnabled(context.getSdkContext());
        alertEnabledHighestGear = preferences.isAudioAlertHighestGearEnabled(context.getSdkContext());
        alertEnabledShiftingLimit = preferences.isAudioAlertShiftingLimit(context.getSdkContext());
        alertEnabledUpcomingSynchroShift = preferences.isAudioAlertUpcomingSynchroShift(context.getSdkContext());

        context.getServiceClient().registerShiftingInfoWeakListener(this::onShifting);
        context.getServiceClient().registerPreferencesWeakListener(this::onPreferences);
    }

    private void onPreferences(PreferencesView preferences) {
        alertEnabledLowestGear = preferences.isAudioAlertLowestGearEnabled(context.getSdkContext());
        alertEnabledHighestGear = preferences.isAudioAlertHighestGearEnabled(context.getSdkContext());
        alertEnabledShiftingLimit = preferences.isAudioAlertShiftingLimit(context.getSdkContext());
        alertEnabledUpcomingSynchroShift = preferences.isAudioAlertUpcomingSynchroShift(context.getSdkContext());
    }

    private void onShifting(DeviceId deviceId, ShiftingInfo shiftingInfo) {
        ShiftingInfo lastShiftingInfo = deviceShiftingMap.put(deviceId, shiftingInfo);

        if (!riding) {
            return;
        }

        if (lastShiftingInfo != null &&
                shiftingInfo.getRearGear() == 1 && shiftingInfo.getFrontGear() == 1 &&
                (lastShiftingInfo.getRearGear() != 1 || lastShiftingInfo.getFrontGear() != 1)) {
            if (alertEnabledLowestGear) {
                AudioAlertHook.triggerGearLimitAudioAlert(context.getSdkContext());
            }
            return;
        }

        if (lastShiftingInfo != null &&
                shiftingInfo.getRearGear() == shiftingInfo.getRearGearMax() && shiftingInfo.getFrontGear() == shiftingInfo.getFrontGearMax() &&
                (lastShiftingInfo.getRearGear() != shiftingInfo.getRearGearMax() || lastShiftingInfo.getFrontGear() != shiftingInfo.getFrontGearMax())) {
            if (alertEnabledHighestGear) {
                AudioAlertHook.triggerGearLimitAudioAlert(context.getSdkContext());
            }
            return;
        }

        if (alertEnabledShiftingLimit && shiftingInfo.getBuzzerType() == BuzzerType.OVERLIMIT_PROTECTION) {
            AudioAlertHook.triggerGearLimitAudioAlert(context.getSdkContext());
            return;
        }

        if (alertEnabledUpcomingSynchroShift && shiftingInfo.getBuzzerType() == BuzzerType.UPCOMING_SYNCHRO_SHIFT) {
            AudioAlertHook.triggerSynchroShiftAudioAlert(context.getSdkContext());
        }
    }

    @Override
    public void onRideStart() {
        riding = true;
    }

    @Override
    public void onRideEnd() {
        riding = false;
    }
}
