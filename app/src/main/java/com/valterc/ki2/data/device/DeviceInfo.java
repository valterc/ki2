package com.valterc.ki2.data.device;

import android.os.Parcel;
import android.os.Parcelable;

import com.valterc.ki2.data.preferences.device.DevicePreferencesView;

public class DeviceInfo implements Parcelable {

    private final DeviceId deviceId;
    private final DevicePreferencesView preferences;

    public static final Parcelable.Creator<DeviceInfo> CREATOR = new Parcelable.Creator<DeviceInfo>() {
        public DeviceInfo createFromParcel(Parcel in) {
            return new DeviceInfo(in);
        }

        public DeviceInfo[] newArray(int size) {
            return new DeviceInfo[size];
        }
    };

    public DeviceInfo(DeviceId deviceId, DevicePreferencesView preferences) {
        this.deviceId = deviceId;
        this.preferences = preferences;
    }

    private DeviceInfo(Parcel in) {
        deviceId = in.readParcelable(DeviceId.class.getClassLoader());
        preferences = in.readParcelable(DevicePreferencesView.class.getClassLoader());
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeParcelable(deviceId, flags);
        out.writeParcelable(preferences, flags);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public DeviceId getId() {
        return deviceId;
    }

    public DevicePreferencesView getPreferences() {
        return preferences;
    }
}
