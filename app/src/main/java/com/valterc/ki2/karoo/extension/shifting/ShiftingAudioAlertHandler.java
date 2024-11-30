package com.valterc.ki2.karoo.extension.shifting;

import com.valterc.ki2.data.device.DeviceId;
import com.valterc.ki2.data.shifting.BuzzerType;
import com.valterc.ki2.data.shifting.ShiftingInfo;
import com.valterc.ki2.karoo.extension.Ki2ExtensionContext;
import com.valterc.ki2.karoo.handlers.IRideHandler;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

@SuppressWarnings("FieldCanBeLocal")
public class ShiftingAudioAlertHandler implements IRideHandler {

    private final Ki2ExtensionContext context;
    private final Map<DeviceId, ShiftingInfo> deviceShiftingMap;
    private boolean riding;
    private final BiConsumer<DeviceId, ShiftingInfo> onShifting = this::onShifting;

    public ShiftingAudioAlertHandler(Ki2ExtensionContext context) {
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
            context.getAudioManager().playLowestGearAudioAlert();
            return;
        }

        if (lastShiftingInfo != null &&
                shiftingInfo.getRearGear() == shiftingInfo.getRearGearMax() && shiftingInfo.getFrontGear() == shiftingInfo.getFrontGearMax() &&
                (lastShiftingInfo.getRearGear() != shiftingInfo.getRearGearMax() || lastShiftingInfo.getFrontGear() != shiftingInfo.getFrontGearMax())) {
            context.getAudioManager().playHighestGearAudioAlert();
            return;
        }

        if (shiftingInfo.getBuzzerType() == BuzzerType.OVERLIMIT_PROTECTION) {
            context.getAudioManager().playShiftingLimitAudioAlert();
            return;
        }

        if (shiftingInfo.getBuzzerType() == BuzzerType.UPCOMING_SYNCHRO_SHIFT) {
            context.getAudioManager().playUpcomingSynchroShiftAudioAlert();
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
