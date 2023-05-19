package com.valterc.ki2.karoo.hooks;

import com.valterc.ki2.data.device.DeviceId;

import io.hammerhead.sdk.v0.SdkContext;

/**
 * FIT file encoder gear shift report hook.
 */
public class FitGearShiftReportHook {

    private FitGearShiftReportHook() {
    }

    /**
     * Initialize DataSyncService hook.
     *
     * @param context Context.
     */
    public static void init(SdkContext context){
        ActivityServiceActivityDataControllerClientHook.preInit(context);
        DataSyncServiceHook.init(context);
    }

    /**
     * Report gear shift event to be stored in the FIT file.
     *
     * @param context        SDK Context.
     * @param deviceId       Device identifier.
     * @param frontGearIndex Front gear index.
     * @param frontGearTeeth Front gear teeth count.
     * @param rearGearIndex  Rear gear index.
     * @param rearGearTeeth  Rear gear teeth count.
     */
    public static void reportGearShift(SdkContext context, DeviceId deviceId, int frontGearIndex, int frontGearTeeth, int rearGearIndex, int rearGearTeeth) {
        boolean result = ActivityServiceActivityDataControllerClientHook.reportGearShift(context, deviceId, frontGearIndex, frontGearTeeth, rearGearIndex, rearGearTeeth);
        if (result) {
            return;
        }

        DataSyncServiceHook.reportGearShift(deviceId, frontGearIndex, frontGearTeeth, rearGearIndex, rearGearTeeth);
    }

}
