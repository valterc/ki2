package com.valterc.ki2.data.connection;

import android.os.Parcel;
import android.os.Parcelable;

import com.valterc.ki2.data.device.DeviceId;
import com.valterc.ki2.data.info.DataInfo;
import com.valterc.ki2.data.info.DataType;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class ConnectionDataInfo implements Parcelable {

    private final DeviceId deviceId;
    private final ConnectionStatus status;
    private final Map<DataType, DataInfo> dataMap;

    public static final Parcelable.Creator<ConnectionDataInfo> CREATOR = new Parcelable.Creator<ConnectionDataInfo>() {
        public ConnectionDataInfo createFromParcel(Parcel in) {
            return new ConnectionDataInfo(in);
        }

        public ConnectionDataInfo[] newArray(int size) {
            return new ConnectionDataInfo[size];
        }
    };

    public ConnectionDataInfo(DeviceId deviceId, ConnectionStatus status, Map<DataType, DataInfo> dataMap) {
        this.deviceId = deviceId;
        this.status = status;
        this.dataMap = dataMap;
    }

    private ConnectionDataInfo(Parcel in) {
        deviceId = in.readParcelable(DeviceId.class.getClassLoader());
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
    public void writeToParcel(Parcel out, int flags) {
        out.writeParcelable(deviceId, flags);
        out.writeInt(status.getValue());

        out.writeInt(dataMap.size());
        for(Map.Entry<DataType, DataInfo> e : dataMap.entrySet()){
            out.writeInt(e.getKey().getFlag());
            out.writeParcelable(e.getValue(), flags);
        }
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

    public ConnectionStatus getConnectionStatus() {
        return status;
    }

    public DeviceId getDeviceId() {
        return deviceId;
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
