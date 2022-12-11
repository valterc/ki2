package com.valterc.ki2.data.update;

import android.os.Parcel;
import android.os.Parcelable;

import java.time.Instant;

public class UpdateInfo implements Parcelable {

    private final String previousVersion;
    private final String newVersion;
    private final Instant updateInstant;
    private final boolean updateFailed;
    private final boolean autoUpdateEnabled;
    private final Instant checkInstant;
    private final boolean updateAvailable;
    private final String updateVersion;

    public static final Parcelable.Creator<UpdateInfo> CREATOR = new Parcelable.Creator<UpdateInfo>() {
        public UpdateInfo createFromParcel(Parcel in) {
            return new UpdateInfo(in);
        }

        public UpdateInfo[] newArray(int size) {
            return new UpdateInfo[size];
        }
    };

    public UpdateInfo(String previousVersion,
                      String newVersion,
                      Instant updateInstant,
                      boolean updateFailed,
                      boolean autoUpdateEnabled,
                      Instant checkInstant,
                      boolean updateAvailable,
                      String updateVersion) {
        this.previousVersion = previousVersion;
        this.newVersion = newVersion;
        this.updateInstant = updateInstant;
        this.updateFailed = updateFailed;
        this.autoUpdateEnabled = autoUpdateEnabled;
        this.checkInstant = checkInstant;
        this.updateAvailable = updateAvailable;
        this.updateVersion = updateVersion;
    }

    private UpdateInfo(Parcel in) {
        this.previousVersion = in.readString();
        this.newVersion = in.readString();
        this.updateInstant = Instant.ofEpochMilli(in.readLong());
        this.updateFailed = in.readByte() == 1;
        this.autoUpdateEnabled = in.readByte() == 1;
        this.checkInstant = Instant.ofEpochMilli(in.readLong());
        this.updateAvailable = in.readByte() == 1;
        this.updateVersion = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(previousVersion);
        dest.writeString(newVersion);
        dest.writeLong(updateInstant.toEpochMilli());
        dest.writeByte(updateFailed ? (byte)1 : 0);
        dest.writeByte(autoUpdateEnabled ? (byte)1 : 0);
        dest.writeLong(checkInstant.toEpochMilli());
        dest.writeByte(updateAvailable ? (byte)1 : 0);
        dest.writeString(updateVersion);
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

    public boolean isUpdateFailed() {
        return updateFailed;
    }

    public Instant getCheckInstant() {
        return checkInstant;
    }

    public boolean isAutoUpdateEnabled() {
        return autoUpdateEnabled;
    }

    public boolean isUpdateAvailable() {
        return updateAvailable;
    }

    public String getUpdateVersion() {
        return updateVersion;
    }

}
