package com.valterc.ki2.data.shifting;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import java.util.Objects;

public final class ShiftingInfo implements Parcelable {

    private final BuzzerType buzzerType;
    private final int frontGear;
    private final int frontGearMax;
    private final int rearGear;
    private final int rearGearMax;
    private final ShiftingMode shiftingMode;
    private final FrontTeethPattern frontTeethPattern;
    private final RearTeethPattern rearTeethPattern;

    public static final Parcelable.Creator<ShiftingInfo> CREATOR = new Parcelable.Creator<ShiftingInfo>() {
        public ShiftingInfo createFromParcel(Parcel in) {
            return new ShiftingInfo(in);
        }

        public ShiftingInfo[] newArray(int size) {
            return new ShiftingInfo[size];
        }
    };

    private ShiftingInfo(Parcel in) {
        buzzerType = BuzzerType.fromCommandNumber(in.readInt());
        frontGear = in.readInt();
        frontGearMax = in.readInt();
        rearGear = in.readInt();
        rearGearMax = in.readInt();
        frontTeethPattern = FrontTeethPattern.fromId(in.readInt());
        rearTeethPattern = RearTeethPattern.fromId(in.readInt());
        shiftingMode = ShiftingMode.fromValue(in.readInt());
    }

    public ShiftingInfo(BuzzerType buzzerType,
                        int frontGear, int frontGearMax, int rearGear, int rearGearMax,
                        FrontTeethPattern frontTeethPattern, RearTeethPattern rearTeethPattern,
                        ShiftingMode shiftingMode)
    {
        this.buzzerType = buzzerType;
        this.frontGear = frontGear;
        this.frontGearMax = frontGearMax;
        this.rearGear = rearGear;
        this.rearGearMax = rearGearMax;
        this.frontTeethPattern = frontTeethPattern;
        this.rearTeethPattern = rearTeethPattern;
        this.shiftingMode = shiftingMode;
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeInt(buzzerType.getCommandNumber());
        out.writeInt(frontGear);
        out.writeInt(frontGearMax);
        out.writeInt(rearGear);
        out.writeInt(rearGearMax);
        out.writeInt(frontTeethPattern.getId());
        out.writeInt(rearTeethPattern.getId());
        out.writeInt(shiftingMode.getValue());
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public BuzzerType getBuzzerType() {
        return buzzerType;
    }

    public int getFrontGear() {
        return frontGear;
    }

    public int getFrontGearMax() {
        return frontGearMax;
    }

    public int getRearGear() {
        return rearGear;
    }

    public int getRearGearMax() {
        return rearGearMax;
    }

    public FrontTeethPattern getFrontTeethPattern() {
        return frontTeethPattern;
    }

    public RearTeethPattern getRearTeethPattern() {
        return rearTeethPattern;
    }

    public ShiftingMode getShiftingMode() {
        return shiftingMode;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ShiftingInfo that = (ShiftingInfo) o;
        return buzzerType == that.buzzerType &&
                frontGear == that.frontGear && frontGearMax == that.frontGearMax &&
                rearGear == that.rearGear && rearGearMax == that.rearGearMax &&
                frontTeethPattern == that.frontTeethPattern && rearTeethPattern == that.rearTeethPattern &&
                shiftingMode == that.shiftingMode;
    }

    @Override
    public int hashCode() {
        return Objects.hash(buzzerType, frontGear, frontGearMax, rearGear, rearGearMax, frontTeethPattern, rearTeethPattern, shiftingMode);
    }

    @NonNull
    @Override
    public String toString() {
        return "ShiftingInfo{" +
                "buzzerType=" + buzzerType +
                ", frontGear=" + frontGear +
                ", frontGearMax=" + frontGearMax +
                ", rearGear=" + rearGear +
                ", rearGearMax=" + rearGearMax +
                ", frontTeethPattern=" + frontTeethPattern +
                ", rearTeethPattern=" + rearTeethPattern +
                ", shiftingMode=" + shiftingMode +
                '}';
    }
}
