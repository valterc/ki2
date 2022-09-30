package com.valterc.ki2.data.info;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Objects;

public class ManufacturerInfo implements Parcelable {

    private String componentId;
    private String hardwareVersion;
    private String manufacturer;
    private String modelNumber;
    private String serialNumber;
    private String softwareVersion;

    public static final Parcelable.Creator<ManufacturerInfo> CREATOR = new Parcelable.Creator<ManufacturerInfo>() {
        public ManufacturerInfo createFromParcel(Parcel in) {
            return new ManufacturerInfo(in);
        }

        public ManufacturerInfo[] newArray(int size) {
            return new ManufacturerInfo[size];
        }
    };

    private ManufacturerInfo(Parcel in) {
        readFromParcel(in);
    }

    public ManufacturerInfo(String componentId, String hardwareVersion, String manufacturer, String modelNumber, String serialNumber, String softwareVersion) {
        this.componentId = componentId;
        this.hardwareVersion = hardwareVersion;
        this.manufacturer = manufacturer;
        this.modelNumber = modelNumber;
        this.serialNumber = serialNumber;
        this.softwareVersion = softwareVersion;
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeString(componentId);
        out.writeString(hardwareVersion);
        out.writeString(manufacturer);
        out.writeString(modelNumber);
        out.writeString(serialNumber);
        out.writeString(softwareVersion);
    }

    public void readFromParcel(Parcel in) {
        componentId = in.readString();
        hardwareVersion = in.readString();
        manufacturer = in.readString();
        modelNumber = in.readString();
        serialNumber = in.readString();
        softwareVersion = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public String getComponentId() {
        return componentId;
    }

    public String getHardwareVersion() {
        return hardwareVersion;
    }

    public String getManufacturer() {
        return manufacturer;
    }

    public String getModelNumber() {
        return modelNumber;
    }

    public String getSerialNumber() {
        return serialNumber;
    }

    public String getSoftwareVersion() {
        return softwareVersion;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ManufacturerInfo that = (ManufacturerInfo) o;
        return Objects.equals(componentId, that.componentId) && Objects.equals(hardwareVersion, that.hardwareVersion) && Objects.equals(manufacturer, that.manufacturer) && Objects.equals(modelNumber, that.modelNumber) && Objects.equals(serialNumber, that.serialNumber) && Objects.equals(softwareVersion, that.softwareVersion);
    }

    @Override
    public int hashCode() {
        return Objects.hash(componentId, hardwareVersion, manufacturer, modelNumber, serialNumber, softwareVersion);
    }
}
