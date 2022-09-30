package com.valterc.ki2.data.connection;

import android.os.Parcel;
import android.os.Parcelable;

import com.valterc.ki2.data.info.DataInfo;
import com.valterc.ki2.data.info.DataType;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class ConnectionDataInfo implements Parcelable {

    public ConnectionStatus status;
    public Map<DataType, DataInfo> dataMap;

    public static final Parcelable.Creator<ConnectionDataInfo> CREATOR = new Parcelable.Creator<ConnectionDataInfo>() {
        public ConnectionDataInfo createFromParcel(Parcel in) {
            return new ConnectionDataInfo(in);
        }

        public ConnectionDataInfo[] newArray(int size) {
            return new ConnectionDataInfo[size];
        }
    };

    public ConnectionDataInfo(ConnectionStatus status, Map<DataType, DataInfo> dataMap) {
        this.status = status;
        this.dataMap = dataMap;
    }

    private ConnectionDataInfo(Parcel in) {
        readFromParcel(in);
    }

    public ConnectionDataInfo(ConnectionStatus status) {
        this.status = status;
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeInt(status.getValue());

        out.writeInt(dataMap.size());
        for(Map.Entry<DataType, DataInfo> e : dataMap.entrySet()){
            out.writeInt(e.getKey().getFlag());
            out.writeParcelable(e.getValue(), flags);
        }
    }

    public void readFromParcel(Parcel in) {
        status = ConnectionStatus.fromValue(in.readInt());

        int size = in.readInt();
        dataMap = new HashMap<>(size);
        for (int i = 0; i < size; i++) {
            DataType dataType = DataType.fromFlag(in.readInt());
            DataInfo value = in.readParcelable(DataInfo.class.getClassLoader());
            dataMap.put(dataType, value);
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public boolean isConnected() {
        return status == ConnectionStatus.ESTABLISHED;
    }

    public boolean isSearching() {
        return status == ConnectionStatus.SEARCHING;
    }

    public ConnectionStatus getStatus() {
        return status;
    }

    public Map<DataType, DataInfo> getDataMap() {
        return dataMap;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ConnectionDataInfo that = (ConnectionDataInfo) o;

        if (status != that.status) return false;
        return Objects.equals(dataMap, that.dataMap);
    }

    @Override
    public int hashCode() {
        int result = status != null ? status.hashCode() : 0;
        result = 31 * result + (dataMap != null ? dataMap.hashCode() : 0);
        return result;
    }

}
