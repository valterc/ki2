package com.valterc.ki2.karoo.shifting;

import com.valterc.ki2.data.device.DeviceId;
import com.valterc.ki2.data.shifting.BuzzerType;
import com.valterc.ki2.data.shifting.ShiftingInfo;
import com.valterc.ki2.karoo.Ki2Context;
import com.valterc.ki2.karoo.handlers.IRideHandler;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

@SuppressWarnings("FieldCanBeLocal")
public class ShiftingAudioAlertHandler implements IRideHandler {

    private final Ki2Context context;
    private final Map<DeviceId, ShiftingInfo> deviceShiftingMap;
    private boolean riding;
    private final BiConsumer<DeviceId, ShiftingInfo> onShifting = this::onShifting;

    public ShiftingAudioAlertHandler(Ki2Context context) {
        this.context = context;
        this.deviceShiftingMap = new HashMap<>();

        context.getServiceClient().registerShiftingInfoWeakListener(onShifting);
    }

    private void onShifting(DeviceId deviceId, ShiftingInfo shiftingInfo) {
        ShiftingInfo lastShiftingInfo = deviceShiftingMap.put(deviceId, shiftingInfo);

        if (!riding) {
            return;
        }

        if (lastShiftingInfo != null &&
                shiftingInfo.getRearGear() == 1 && shiftingInfo.getFrontGear() == 1 &&
                (lastShiftingInfo.getRearGear() != 1 || lastShiftingInfo.getFrontGear() != 1)) {
            context.getAudioAlertManager().triggerShiftingLowestGearAudioAlert();
            return;
        }

        if (lastShiftingInfo != null &&
                shiftingInfo.getRearGear() == shiftingInfo.getRearGearMax() && shiftingInfo.getFrontGear() == shiftingInfo.getFrontGearMax() &&
                (lastShiftingInfo.getRearGear() != shiftingInfo.getRearGearMax() || lastShiftingInfo.getFrontGear() != shiftingInfo.getFrontGearMax())) {
            context.getAudioAlertManager().triggerShiftingHighestGearAudioAlert();
            return;
        }

        if (shiftingInfo.getBuzzerType() == BuzzerType.OVERLIMIT_PROTECTION) {
            context.getAudioAlertManager().triggerShiftingLimitAudioAlert();
            return;
        }

        if (shiftingInfo.getBuzzerType() == BuzzerType.UPCOMING_SYNCHRO_SHIFT) {
            context.getAudioAlertManager().triggerSynchroShiftAudioAlert();
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
