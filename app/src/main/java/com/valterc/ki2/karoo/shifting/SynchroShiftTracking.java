package com.valterc.ki2.karoo.shifting;

import com.valterc.ki2.data.shifting.BuzzerType;
import com.valterc.ki2.data.shifting.ShiftingInfo;
import com.valterc.ki2.data.shifting.UpcomingSynchroShiftType;

public class SynchroShiftTracking {

    private ShiftingInfo lastShiftingInfo;

    private UpcomingSynchroShiftType upcomingSynchroShiftType;

    public SynchroShiftTracking() {
        upcomingSynchroShiftType = UpcomingSynchroShiftType.NONE;
    }

    public void setShiftingInfo(ShiftingInfo shiftingInfo) {
        try {
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

    public boolean isUpcomingSynchroShift() {
        return upcomingSynchroShiftType != UpcomingSynchroShiftType.NONE;
    }

}
