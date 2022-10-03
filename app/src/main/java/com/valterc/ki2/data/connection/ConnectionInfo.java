package com.valterc.ki2.data.connection;

import android.os.Parcel;
import android.os.Parcelable;

public class ConnectionInfo implements Parcelable {

    private ConnectionStatus status;

    public static final Parcelable.Creator<ConnectionInfo> CREATOR = new Parcelable.Creator<ConnectionInfo>() {
        public ConnectionInfo createFromParcel(Parcel in) {
            return new ConnectionInfo(in);
        }

        public ConnectionInfo[] newArray(int size) {
            return new ConnectionInfo[size];
        }
    };

    private ConnectionInfo(Parcel in) {
        readFromParcel(in);
    }

    public ConnectionInfo(ConnectionStatus status) {
        this.status = status;
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeInt(status.getValue());
    }

    public void readFromParcel(Parcel in) {
        status = ConnectionStatus.fromValue(in.readInt());
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public boolean isConnected() {
        return status == ConnectionStatus.ESTABLISHED;
    }

    public boolean isConnecting() {
        return status == ConnectionStatus.CONNECTING;
    }

    public boolean isNewOrConnecting() {
        return status == ConnectionStatus.NEW || status == ConnectionStatus.CONNECTING;
    }

    public ConnectionStatus getStatus() {
        return status;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ConnectionInfo that = (ConnectionInfo) o;

        return status == that.status;
    }

    @Override
    public int hashCode() {
        return status != null ? status.hashCode() : 0;
    }
}
