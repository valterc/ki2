package com.valterc.ki2.karoo.extension.shifting;

import android.content.Context;

import com.valterc.ki2.data.preferences.device.DevicePreferencesView;
import com.valterc.ki2.data.shifting.ShiftingInfo;

public class ShiftingGearingHelper {

    private final Context context;
    private ShiftingInfo shiftingInfo;
    private DevicePreferencesView devicePreferences;

    private boolean customGearing;
    private int[] customGearingFront;
    private int[] customGearingRear;

    private int gearTeethCountFront;
    private int gearTeethCountRear;

    public ShiftingGearingHelper(Context context) {
        this.context = context;
    }

    private void updateGearing() {
        if (shiftingInfo == null) {
            return;
        }

        if (!customGearing) {
            gearTeethCountFront = shiftingInfo.getFrontTeethPattern().getTeethCount(shiftingInfo.getFrontGear());
            gearTeethCountRear = shiftingInfo.getRearTeethPattern().getTeethCount(shiftingInfo.getRearGear());
        } else {
            int frontGearIndex = shiftingInfo.getFrontGear() - 1;
            int rearGearIndex = shiftingInfo.getRearGear() - 1;
            gearTeethCountFront = customGearingFront == null || frontGearIndex < 0 || frontGearIndex >= customGearingFront.length ? 0 : customGearingFront[frontGearIndex];
            gearTeethCountRear = customGearingRear == null || rearGearIndex < 0 || rearGearIndex >= customGearingRear.length ? 0 : customGearingRear[rearGearIndex];
        }
    }

    private void updateGearingPreferences() {
        if (devicePreferences == null) {
            return;
        }

        customGearing = !devicePreferences.isGearingDetectedAutomatically(context);
        if (customGearing) {
            customGearingFront = devicePreferences.getCustomGearingFront(context);
            customGearingRear = devicePreferences.getCustomGearingRear(context);
        }
    }

    public void setShiftingInfo(ShiftingInfo shiftingInfo) {
        if (this.shiftingInfo == shiftingInfo) {
            return;
        }

        this.shiftingInfo = shiftingInfo;
        updateGearing();
    }

    public void setDevicePreferences(DevicePreferencesView devicePreferences) {
        if (this.devicePreferences == devicePreferences) {
            return;
        }

        this.devicePreferences = devicePreferences;
        updateGearingPreferences();
        updateGearing();
    }

    public boolean hasInvalidGearingInfo() {
        return shiftingInfo == null || devicePreferences == null;
    }

    public boolean hasValidGearingInfo() {
        return shiftingInfo != null && devicePreferences != null;
    }

    public boolean hasFrontGearSize() {
        return gearTeethCountFront != 0;
    }

    public boolean hasRearGearSize() {
        return gearTeethCountRear != 0;
    }

    public boolean hasGearSizes() {
        return gearTeethCountFront != 0 && gearTeethCountRear != 0;
    }

    public int getFrontGearTeethCount() {
        return gearTeethCountFront;
    }

    public int getRearGearTeethCount() {
        return gearTeethCountRear;
    }

    public float getGearRatio() {
        return gearTeethCountRear == 0 ? 0 : (float) gearTeethCountFront / gearTeethCountRear;
    }

    public int getFrontGear() {
        return shiftingInfo == null ? 0 : shiftingInfo.getFrontGear();
    }

    public int getRearGear() {
        return shiftingInfo == null ? 0 : shiftingInfo.getRearGear();
    }

    public int getFrontGearMax() {
        return shiftingInfo == null ? 0 : shiftingInfo.getFrontGearMax();
    }

    public int getRearGearMax() {
        return shiftingInfo == null ? 0 : shiftingInfo.getRearGearMax();
    }

}
