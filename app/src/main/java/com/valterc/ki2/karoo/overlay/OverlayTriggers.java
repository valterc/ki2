package com.valterc.ki2.karoo.overlay;

import com.valterc.ki2.data.connection.ConnectionInfo;
import com.valterc.ki2.data.device.BatteryInfo;
import com.valterc.ki2.data.shifting.BuzzerType;
import com.valterc.ki2.data.shifting.ShiftingInfo;

import java.util.Set;

public class OverlayTriggers {

    private static final String TRIGGER_PRIMARY_HIDDEN = "primary-hidden";
    private static final String TRIGGER_CONNECTION = "connection";
    private static final String TRIGGER_BATTERY = "battery";
    private static final String TRIGGER_SHIFTING = "shifting";
    private static final String TRIGGER_UPCOMING_SYNCHRO_SHIFT = "upcoming-synchro-shift";

    private final Set<String> triggers;
    private boolean shouldShowOverlay;

    public OverlayTriggers(Set<String> triggers) {
        this.triggers = triggers;
    }

    public boolean isTriggeredByPrimaryHidden(){
        return triggers.contains(TRIGGER_PRIMARY_HIDDEN);
    }

    public void onPrimaryHidden() {
        shouldShowOverlay |= triggers.contains(TRIGGER_PRIMARY_HIDDEN);
    }

    public void onConnectionInfo(ConnectionInfo connectionInfo) {
        shouldShowOverlay |= triggers.contains(TRIGGER_CONNECTION);
    }

    public void onShiftingInfo(ShiftingInfo shiftingInfo) {
        shouldShowOverlay |= (triggers.contains(TRIGGER_SHIFTING) ||
                (shiftingInfo.getBuzzerType() == BuzzerType.UPCOMING_SYNCHRO_SHIFT && triggers.contains(TRIGGER_UPCOMING_SYNCHRO_SHIFT)));
    }

    public void onBatteryInfo(BatteryInfo batteryInfo) {
        shouldShowOverlay |= triggers.contains(TRIGGER_BATTERY);
    }

    public boolean queryAndClearShouldShowOverlay() {
        try {
            return shouldShowOverlay;
        } finally {
            shouldShowOverlay = false;
        }
    }

}
