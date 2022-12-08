package com.valterc.ki2.data.update;

import android.os.Parcel;
import android.os.Parcelable;

import java.time.Instant;

public class UpdateStateInfo implements Parcelable {

    private final String previousVersion;
    private final String newVersion;
    private final Instant updateInstant;

    public static final Parcelable.Creator<UpdateStateInfo> CREATOR = new Parcelable.Creator<UpdateStateInfo>() {
        public UpdateStateInfo createFromParcel(Parcel in) {
            return new UpdateStateInfo(in);
        }

        public UpdateStateInfo[] newArray(int size) {
            return new UpdateStateInfo[size];
        }
    };

    public UpdateStateInfo(String previousVersion, String newVersion, Instant updateInstant) {
        this.previousVersion = previousVersion;
        this.newVersion = newVersion;
        this.updateInstant = updateInstant;
    }

    private UpdateStateInfo(Parcel in) {
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
