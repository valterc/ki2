package com.valterc.ki2.data.switches;

import android.content.Context;

import androidx.annotation.NonNull;

import com.valterc.ki2.R;

public final class WirelessSwitchName {

    private WirelessSwitchName() {
    }

    @NonNull
    public static String getName(Context context, WirelessSwitchInfo wirelessSwitchInfo) {
        String typeName = switch (wirelessSwitchInfo.getType()) {
            case ROAD_SHIFTER -> context.getString(R.string.text_road_shifter);
            case TT_SHIFTER -> context.getString(R.string.text_tt_shifter);
            case TT_SWITCH -> context.getString(R.string.text_tt_switch);
            case MTB_SWITCH -> context.getString(R.string.text_mtb_switch);
            default -> context.getString(R.string.text_switch);
        };

        return switch (wirelessSwitchInfo.getSide()) {
            case LEFT -> context.getString(R.string.text_param_side_switch, context.getString(R.string.text_left), typeName);
            case RIGHT -> context.getString(R.string.text_param_side_switch, context.getString(R.string.text_right), typeName);
            default -> typeName;
        };
    }

}
