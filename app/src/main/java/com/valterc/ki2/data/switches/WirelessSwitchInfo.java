package com.valterc.ki2.data.switches;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

public class WirelessSwitchInfo implements Parcelable {

    private final int slot;
    private final SwitchType type;
    private final SwitchSide side;
    private final WirelessSwitchBatteryLevel batteryLevel;

    public static final Parcelable.Creator<WirelessSwitchInfo> CREATOR = new Parcelable.Creator<WirelessSwitchInfo>() {
        public WirelessSwitchInfo createFromParcel(Parcel in) {
            return new WirelessSwitchInfo(in);
        }

        public WirelessSwitchInfo[] newArray(int size) {
            return new WirelessSwitchInfo[size];
        }
    };

    private WirelessSwitchInfo(Parcel in) {
        slot = in.readInt();
        type = SwitchType.fromValue(in.readInt());
        side = SwitchSide.fromValue(in.readInt());
        batteryLevel = WirelessSwitchBatteryLevel.fromValue(in.readInt());
    }

    /**
     * Create a new wireless switch info.
     *
     * @param slot         Wireless switch slot as reported by the Di2 system (1 to 4).
     * @param type         Type of switch.
     * @param side         Side the switch is assigned to.
     * @param batteryLevel Battery level of the switch.
     */
    public WirelessSwitchInfo(int slot, SwitchType type, SwitchSide side, WirelessSwitchBatteryLevel batteryLevel) {
        this.slot = slot;
        this.type = type;
        this.side = side;
        this.batteryLevel = batteryLevel;
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeInt(slot);
        out.writeInt(type.getValue());
        out.writeInt(side.getValue());
        out.writeInt(batteryLevel.getValue());
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public int getSlot() {
        return slot;
    }

    public SwitchType getType() {
        return type;
    }

    public SwitchSide getSide() {
        return side;
    }

    public WirelessSwitchBatteryLevel getBatteryLevel() {
        return batteryLevel;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        WirelessSwitchInfo that = (WirelessSwitchInfo) o;

        if (slot != that.slot) return false;
        if (type != that.type) return false;
        if (side != that.side) return false;
        return batteryLevel == that.batteryLevel;
    }

    @Override
    public int hashCode() {
        int result = slot;
        result = 31 * result + (type != null ? type.hashCode() : 0);
        result = 31 * result + (side != null ? side.hashCode() : 0);
        result = 31 * result + (batteryLevel != null ? batteryLevel.hashCode() : 0);
        return result;
    }

    @NonNull
    @Override
    public String toString() {
        return "WirelessSwitchInfo{" +
                "slot=" + slot +
                ", type=" + type +
                ", side=" + side +
                ", batteryLevel=" + batteryLevel +
                '}';
    }
}
