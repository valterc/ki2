package com.valterc.ki2.views.battery;

import android.content.res.ColorStateList;
import android.widget.TextView;

import androidx.annotation.ColorRes;
import androidx.annotation.DrawableRes;
import androidx.annotation.Nullable;
import androidx.core.widget.TextViewCompat;

import com.valterc.ki2.R;
import com.valterc.ki2.data.switches.WirelessSwitchBatteryLevel;

public enum BatteryIndicator {

    LEVEL_5(R.color.hh_green, R.drawable.ic_battery_5),
    LEVEL_4(R.color.hh_green, R.drawable.ic_battery_4),
    LEVEL_3(R.color.hh_green, R.drawable.ic_battery_3),
    LEVEL_2(R.color.hh_yellow_dark, R.drawable.ic_battery_2),
    LEVEL_1(R.color.hh_orange_dark, R.drawable.ic_battery_1),
    LEVEL_0(R.color.hh_red, R.drawable.ic_battery_0);

    public static BatteryIndicator fromPercentage(int percentage) {
        if (percentage >= 80) {
            return LEVEL_5;
        } else if (percentage >= 70) {
            return LEVEL_4;
        } else if (percentage >= 50) {
            return LEVEL_3;
        } else if (percentage >= 30) {
            return LEVEL_2;
        } else if (percentage >= 20) {
            return LEVEL_1;
        }

        return LEVEL_0;
    }

    @Nullable
    public static BatteryIndicator fromWirelessSwitchBatteryLevel(WirelessSwitchBatteryLevel wirelessSwitchBatteryLevel) {
        switch (wirelessSwitchBatteryLevel) {
            case HIGH:
                return LEVEL_5;
            case MID:
                return LEVEL_3;
            case LOW:
                return LEVEL_1;
            default:
                return null;
        }
    }

    private final int colorId;
    private final int drawableId;

    BatteryIndicator(@ColorRes int colorId, @DrawableRes int drawableId) {
        this.colorId = colorId;
        this.drawableId = drawableId;
    }

    @ColorRes
    public int getColorId() {
        return colorId;
    }

    @DrawableRes
    public int getDrawableId() {
        return drawableId;
    }

    /**
     * Set the battery icon as the end compound drawable of the text view, tinted with the indicator color.
     *
     * @param textView Text view to update.
     */
    public void applyTo(TextView textView) {
        TextViewCompat.setCompoundDrawablesRelativeWithIntrinsicBounds(textView, 0, 0, drawableId, 0);
        TextViewCompat.setCompoundDrawableTintList(textView, ColorStateList.valueOf(textView.getContext().getColor(colorId)));
    }
}
