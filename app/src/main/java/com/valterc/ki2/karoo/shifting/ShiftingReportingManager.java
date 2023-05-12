package com.valterc.ki2.karoo.shifting;

import com.valterc.ki2.data.device.DeviceId;
import com.valterc.ki2.data.preferences.device.DevicePreferencesView;
import com.valterc.ki2.data.shifting.ShiftingInfo;
import com.valterc.ki2.karoo.Ki2Context;
import com.valterc.ki2.karoo.handlers.IRideHandler;
import com.valterc.ki2.karoo.hooks.DataSyncServiceHook;

import java.util.function.BiConsumer;

@SuppressWarnings("FieldCanBeLocal")
public class ShiftingReportingManager implements IRideHandler {

    private boolean riding;
    private final ShiftingGearingHelper shiftingGearingHelper;

    private final BiConsumer<DeviceId, ShiftingInfo> onShifting = this::onShifting;
    private final BiConsumer<DeviceId, DevicePreferencesView> onDevicePreferences = this::onDevicePreferences;

    public ShiftingReportingManager(Ki2Context ki2Context) {
        DataSyncServiceHook.init(ki2Context.getSdkContext());
        shiftingGearingHelper = new ShiftingGearingHelper(ki2Context.getSdkContext());

        ki2Context.getServiceClient().registerShiftingInfoWeakListener(onShifting);
        ki2Context.getServiceClient().registerDevicePreferencesWeakListener(onDevicePreferences);
    }

    private void onShifting(DeviceId deviceId, ShiftingInfo shiftingInfo) {
        shiftingGearingHelper.setShiftingInfo(shiftingInfo);

        if (riding && shiftingGearingHelper.hasValidGearingInfo()) {
            DataSyncServiceHook.reportGearShift(
                    shiftingGearingHelper.getFrontGear(),
                    shiftingGearingHelper.getFrontGearTeethCount(),
                    shiftingGearingHelper.getRearGear(),
                    shiftingGearingHelper.getRearGearTeethCount());
        }
    }

    private void onDevicePreferences(DeviceId deviceId, DevicePreferencesView devicePreferencesView) {
        shiftingGearingHelper.setDevicePreferences(devicePreferencesView);

        if (riding && shiftingGearingHelper.hasValidGearingInfo()) {
            DataSyncServiceHook.reportGearShift(
                    shiftingGearingHelper.getFrontGear(),
                    shiftingGearingHelper.getFrontGearTeethCount(),
                    shiftingGearingHelper.getRearGear(),
                    shiftingGearingHelper.getRearGearTeethCount());
        }
    }

    @Override
    public void onRideStart() {
        riding = true;

        if (shiftingGearingHelper.hasValidGearingInfo()) {
            DataSyncServiceHook.reportGearShift(
                    shiftingGearingHelper.getFrontGear(),
                    shiftingGearingHelper.getFrontGearTeethCount(),
                    shiftingGearingHelper.getRearGear(),
                    shiftingGearingHelper.getRearGearTeethCount());
        }
    }

    @Override
    public void onRideEnd() {
        riding = false;
    }
}
