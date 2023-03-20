package com.valterc.ki2.karoo.shifting;

import com.valterc.ki2.data.shifting.BuzzerType;
import com.valterc.ki2.data.shifting.ShiftingInfo;
import com.valterc.ki2.data.shifting.ShiftingLimitType;
import com.valterc.ki2.data.shifting.UpcomingSynchroShiftType;

public class BuzzerTracking {

    private ShiftingInfo lastShiftingInfo;

    private UpcomingSynchroShiftType upcomingSynchroShiftType;
    private ShiftingLimitType shiftingLimitType;

    public BuzzerTracking() {
        upcomingSynchroShiftType = UpcomingSynchroShiftType.NONE;
        shiftingLimitType = ShiftingLimitType.NONE;
    }

    public void setShiftingInfo(ShiftingInfo shiftingInfo) {
        try {
            if (shiftingInfo != null) {
                if (shiftingInfo.getRearGear() == shiftingInfo.getRearGearMax() &&
                        shiftingInfo.getFrontGear() == shiftingInfo.getFrontGearMax()) {
                    shiftingLimitType = ShiftingLimitType.HIGHER_LIMIT;
                } else if (shiftingInfo.getRearGear() == 1 &&
                        shiftingInfo.getFrontGear() == 1) {
                    shiftingLimitType = ShiftingLimitType.LOWER_LIMIT;
                } else {
                    shiftingLimitType = ShiftingLimitType.NONE;
                }
            } else {
                shiftingLimitType = ShiftingLimitType.NONE;
            }

            if (shiftingInfo == null || lastShiftingInfo == null) {
                upcomingSynchroShiftType = UpcomingSynchroShiftType.NONE;
                return;
            }

            if (shiftingInfo.getBuzzerType() != BuzzerType.UPCOMING_SYNCHRO_SHIFT) {
                if (lastShiftingInfo.getRearGear() != shiftingInfo.getRearGear() || lastShiftingInfo.getFrontGear() != shiftingInfo.getFrontGear()) {
                    upcomingSynchroShiftType = UpcomingSynchroShiftType.NONE;
                }
                return;
            }

            if (shiftingInfo.getRearGear() != lastShiftingInfo.getRearGear()) {
                upcomingSynchroShiftType = shiftingInfo.getRearGear() > lastShiftingInfo.getRearGear() ? UpcomingSynchroShiftType.UPCOMING_UP : UpcomingSynchroShiftType.UPCOMING_DOWN;
            } else {
                upcomingSynchroShiftType = shiftingInfo.getFrontGear() > lastShiftingInfo.getFrontGear() ? UpcomingSynchroShiftType.UPCOMING_DOWN : UpcomingSynchroShiftType.UPCOMING_UP;
            }
        } finally {
            lastShiftingInfo = shiftingInfo;
        }
    }

    public UpcomingSynchroShiftType getUpcomingSynchroShiftType() {
        return upcomingSynchroShiftType;
    }

    public ShiftingLimitType getShiftingLimitType() {
        return shiftingLimitType;
    }

    public boolean isUpcomingSynchroShift() {
        return upcomingSynchroShiftType != UpcomingSynchroShiftType.NONE;
    }

    public boolean isShiftingLimit() {
        return shiftingLimitType != ShiftingLimitType.NONE;
    }

}
