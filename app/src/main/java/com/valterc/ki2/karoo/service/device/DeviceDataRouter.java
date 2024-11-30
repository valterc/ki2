package com.valterc.ki2.karoo.service.device;

import com.valterc.ki2.data.connection.ConnectionInfo;
import com.valterc.ki2.data.device.BatteryInfo;
import com.valterc.ki2.data.device.DeviceId;
import com.valterc.ki2.data.input.KarooKeyEvent;
import com.valterc.ki2.data.preferences.device.DevicePreferencesView;
import com.valterc.ki2.data.shifting.ShiftingInfo;
import com.valterc.ki2.karoo.service.listeners.BiDataStreamWeakListenerList;

import java.util.function.BiConsumer;

public class DeviceDataRouter {

    private final BiDataStreamWeakListenerList<DeviceId, ConnectionInfo> connectionInfoListeners;
    private final BiDataStreamWeakListenerList<DeviceId, BatteryInfo> batteryInfoListeners;
    private final BiDataStreamWeakListenerList<DeviceId, ShiftingInfo> shiftingInfoListeners;
    private final BiDataStreamWeakListenerList<DeviceId, DevicePreferencesView> devicePreferencesListeners;
    private final BiDataStreamWeakListenerList<DeviceId, KarooKeyEvent> keyEventListeners;

    public DeviceDataRouter() {
        connectionInfoListeners = new BiDataStreamWeakListenerList<>();
        batteryInfoListeners = new BiDataStreamWeakListenerList<>();
        shiftingInfoListeners = new BiDataStreamWeakListenerList<>();
        devicePreferencesListeners = new BiDataStreamWeakListenerList<>();
        keyEventListeners = new BiDataStreamWeakListenerList<>();
    }

    public void registerConnectionInfoWeakListener(BiConsumer<DeviceId, ConnectionInfo> connectionInfoConsumer) {
        connectionInfoListeners.addListener(connectionInfoConsumer);
    }

    public boolean hasConnectionInfoListeners() {
        return connectionInfoListeners.hasListeners();
    }

    public void registerBatteryInfoWeakListener(BiConsumer<DeviceId, BatteryInfo> batteryInfoConsumer) {
        batteryInfoListeners.addListener(batteryInfoConsumer);
    }

    public boolean hasBatteryInfoListeners() {
        return batteryInfoListeners.hasListeners();
    }

    public void registerShiftingInfoWeakListener(BiConsumer<DeviceId, ShiftingInfo> shiftingInfoConsumer) {
        shiftingInfoListeners.addListener(shiftingInfoConsumer);
    }

    public boolean hasShiftingInfoListeners() {
        return shiftingInfoListeners.hasListeners();
    }

    public void registerDevicePreferencesWeakListener(BiConsumer<DeviceId, DevicePreferencesView> devicePreferencesConsumer) {
        devicePreferencesListeners.addListener(devicePreferencesConsumer);
    }

    public boolean hasDevicePreferencesListeners() {
        return devicePreferencesListeners.hasListeners();
    }

    public void registerKeyEventListener(BiConsumer<DeviceId, KarooKeyEvent> keyEventConsumer) {
        keyEventListeners.addListener(keyEventConsumer);
    }

    public boolean hasKeyListeners() {
        return keyEventListeners.hasListeners();
    }

    public void onConnectionInfo(DeviceId deviceId, ConnectionInfo connectionInfo) {
        connectionInfoListeners.pushData(deviceId, connectionInfo);
    }

    public void onBattery(DeviceId deviceId, BatteryInfo batteryInfo) {
        batteryInfoListeners.pushData(deviceId, batteryInfo);
    }

    public void onShifting(DeviceId deviceId, ShiftingInfo shiftingInfo) {
        shiftingInfoListeners.pushData(deviceId, shiftingInfo);
    }

    public void onDevicePreferences(DeviceId deviceId, DevicePreferencesView preferences) {
        devicePreferencesListeners.pushData(deviceId, preferences);
    }

    public void onKeyEvent(DeviceId deviceId, KarooKeyEvent keyEvent) {
        keyEventListeners.pushData(deviceId, keyEvent);
    }

}
