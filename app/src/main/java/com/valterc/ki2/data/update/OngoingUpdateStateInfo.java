package com.valterc.ki2.data.update;

import android.os.Parcel;
import android.os.Parcelable;

import java.time.Instant;

public class OngoingUpdateStateInfo implements Parcelable {

    private final String previousVersion;
    private final String newVersion;
    private final Instant updateInstant;

    public static final Parcelable.Creator<OngoingUpdateStateInfo> CREATOR = new Parcelable.Creator<OngoingUpdateStateInfo>() {
        public OngoingUpdateStateInfo createFromParcel(Parcel in) {
            return new OngoingUpdateStateInfo(in);
        }

        public OngoingUpdateStateInfo[] newArray(int size) {
            return new OngoingUpdateStateInfo[size];
        }
    };

    public OngoingUpdateStateInfo(String previousVersion, String newVersion, Instant updateInstant) {
        this.previousVersion = previousVersion;
        this.newVersion = newVersion;
        this.updateInstant = updateInstant;
    }

    private OngoingUpdateStateInfo(Parcel in) {
        this.previousVersion = in.readString();
        this.newVersion = in.readString();
        this.updateInstant = Instant.ofEpochMilli(in.readLong());
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(previousVersion);
        dest.writeString(newVersion);
        dest.writeLong(updateInstant.toEpochMilli());
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public String getPreviousVersion() {
        return previousVersion;
    }

    public String getNewVersion() {
        return newVersion;
    }

    public Instant getUpdateInstant() {
        return updateInstant;
    }

}
