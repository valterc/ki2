package com.valterc.ki2.external;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class ExternalAction {

    public static final int SWITCH_CH1 = 1;
    public static final int SWITCH_CH2 = 1 << 1;
    public static final int SWITCH_CH3 = 1 << 2;
    public static final int SWITCH_CH4 = 1 << 3;
    public static final int SWITCH_ALL = SWITCH_CH1 | SWITCH_CH2 | SWITCH_CH3 | SWITCH_CH4;

    @NonNull
    private final String actionId;
    @NonNull
    private final String label;
    @Nullable
    private final String iconUri;
    private final int allowedSwitches;

    public ExternalAction(@NonNull String actionId, @NonNull String label, @Nullable String iconUri, int allowedSwitches) {
        this.actionId = actionId;
        this.label = label;
        this.iconUri = iconUri;
        this.allowedSwitches = allowedSwitches;
    }

    @NonNull
    public String getActionId() {
        return actionId;
    }

    @NonNull
    public String getLabel() {
        return label;
    }

    @Nullable
    public String getIconUri() {
        return iconUri;
    }

    public int getAllowedSwitches() {
        return allowedSwitches == 0 ? SWITCH_ALL : allowedSwitches;
    }
}
