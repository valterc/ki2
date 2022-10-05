package com.valterc.ki2.data.info;

public class ManufacturerInfoBuilder {

    private String componentId;
    private String hardwareVersion;
    private Manufacturer manufacturer;
    private String modelNumber;
    private String serialNumber;
    private String softwareVersion;

    private boolean componentIdSet;
    private boolean hardwareVersionSet;
    private boolean manufacturerSet;
    private boolean modelNumberSet;
    private boolean serialNumberSet;
    private boolean softwareVersionSet;

    public ManufacturerInfoBuilder() {
    }

    public String getComponentId() {
        return componentId;
    }

    public void setComponentId(String componentId) {
        this.componentId = componentId;
        this.componentIdSet = true;
    }

    public String getHardwareVersion() {
        return hardwareVersion;
    }

    public void setHardwareVersion(String hardwareVersion) {
        this.hardwareVersion = hardwareVersion;
        this.hardwareVersionSet = true;
    }

    public Manufacturer getManufacturer() {
        return manufacturer;
    }

    public void setManufacturer(Manufacturer manufacturer) {
        this.manufacturer = manufacturer;
        this.manufacturerSet = true;
    }

    public String getModelNumber() {
        return modelNumber;
    }

    public void setModelNumber(String modelNumber) {
        this.modelNumber = modelNumber;
        this.modelNumberSet = true;
    }

    public String getSerialNumber() {
        return serialNumber;
    }

    public void setSerialNumber(String serialNumber) {
        this.serialNumber = serialNumber;
        this.serialNumberSet = true;
    }

    public String getSoftwareVersion() {
        return softwareVersion;
    }

    public void setSoftwareVersion(String softwareVersion) {
        this.softwareVersion = softwareVersion;
        this.softwareVersionSet = true;
    }

    public boolean allSet(){
        return componentIdSet &&
                hardwareVersionSet &&
                manufacturerSet &&
                modelNumberSet &&
                serialNumberSet &&
                softwareVersionSet;
    }

    public ManufacturerInfo build(){
        return new ManufacturerInfo(componentId, hardwareVersion, manufacturer, modelNumber, serialNumber, softwareVersion);
    }
}
