package com.valterc.ki2.data.switches;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class WirelessSwitchesInfo implements Parcelable {

    private final List<WirelessSwitchInfo> switches;

    public static final Parcelable.Creator<WirelessSwitchesInfo> CREATOR = new Parcelable.Creator<WirelessSwitchesInfo>() {
        public WirelessSwitchesInfo createFromParcel(Parcel in) {
            return new WirelessSwitchesInfo(in);
        }

        public WirelessSwitchesInfo[] newArray(int size) {
            return new WirelessSwitchesInfo[size];
        }
    };

    private WirelessSwitchesInfo(Parcel in) {
        List<WirelessSwitchInfo> list = new ArrayList<>();
        in.readTypedList(list, WirelessSwitchInfo.CREATOR);
        switches = Collections.unmodifiableList(list);
    }

    public WirelessSwitchesInfo(List<WirelessSwitchInfo> switches) {
        this.switches = switches.stream()
                .filter(wirelessSwitchInfo -> wirelessSwitchInfo.getType() != SwitchType.NONE && wirelessSwitchInfo.getType() != SwitchType.UNKNOWN)
                .collect(Collectors.collectingAndThen(Collectors.toList(), Collections::unmodifiableList));
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeTypedList(switches);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public List<WirelessSwitchInfo> getSwitches() {
        return switches;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        WirelessSwitchesInfo that = (WirelessSwitchesInfo) o;

        return switches.equals(that.switches);
    }

    @Override
    public int hashCode() {
        return switches.hashCode();
    }

    @NonNull
    @Override
    public String toString() {
        return "WirelessSwitchesInfo{" +
                "switches=" + switches +
                '}';
    }
}
