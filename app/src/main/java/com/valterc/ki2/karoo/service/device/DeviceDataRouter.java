package com.valterc.ki2.karoo.service.device;

import android.content.Context;

import com.valterc.ki2.data.connection.ConnectionInfo;
import com.valterc.ki2.data.connection.ConnectionStatus;
import com.valterc.ki2.data.device.BatteryInfo;
import com.valterc.ki2.data.device.DeviceId;
import com.valterc.ki2.data.preferences.device.DevicePreferencesView;
import com.valterc.ki2.data.shifting.ShiftingInfo;
import com.valterc.ki2.karoo.service.listeners.BiDataStreamWeakListenerList;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

public class DeviceDataRouter {

    private final BiDataStreamWeakListenerList<DeviceId, ConnectionInfo> connectionInfoListeners;
    private final BiDataStreamWeakListenerList<DeviceId, BatteryInfo> batteryInfoListeners;
    private final BiDataStreamWeakListenerList<DeviceId, BatteryInfo> batteryInfoUnfilteredListeners;
    private final BiDataStreamWeakListenerList<DeviceId, ShiftingInfo> shiftingInfoListeners;
    private final BiDataStreamWeakListenerList<DeviceId, DevicePreferencesView> devicePreferencesListener;

    private final Context context;
    private final Map<DeviceId, DeviceData> deviceDataMap;
    private DeviceId currentDeviceId;

    public DeviceDataRouter(Context context) {
        this.context = context;

        connectionInfoListeners = new BiDataStreamWeakListenerList<>();
        batteryInfoListeners = new BiDataStreamWeakListenerList<>();
        batteryInfoUnfilteredListeners = new BiDataStreamWeakListenerList<>();
        shiftingInfoListeners = new BiDataStreamWeakListenerList<>();
        devicePreferencesListener = new BiDataStreamWeakListenerList<>();

        deviceDataMap = new HashMap<>();
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

    public void registerUnfilteredBatteryInfoWeakListener(BiConsumer<DeviceId, BatteryInfo> batteryInfoConsumer) {
        batteryInfoUnfilteredListeners.addListener(batteryInfoConsumer);
    }

    public boolean hasBatteryInfoListeners() {
        return batteryInfoListeners.hasListeners() || batteryInfoUnfilteredListeners.hasListeners();
    }

    public void registerShiftingInfoWeakListener(BiConsumer<DeviceId, ShiftingInfo> shiftingInfoConsumer) {
        shiftingInfoListeners.addListener(shiftingInfoConsumer);
    }

    public boolean hasShiftingInfoListeners() {
        return shiftingInfoListeners.hasListeners();
    }

    public void registerDevicePreferencesWeakListener(BiConsumer<DeviceId, DevicePreferencesView> devicePreferencesConsumer) {
        devicePreferencesListener.addListener(devicePreferencesConsumer);
    }

    public boolean hasDevicePreferencesListeners() {
        return devicePreferencesListener.hasListeners();
    }

    private void attemptToUpdateCurrentDevice() {
        if (tryUpdateCurrentDevice()) {
            DeviceData newDeviceData = deviceDataMap.computeIfAbsent(currentDeviceId, DeviceData::new);

            if (newDeviceData.getConnectionInfo() != null) {
                connectionInfoListeners.pushData(currentDeviceId, newDeviceData.getConnectionInfo());
            }

            if (newDeviceData.getBatteryInfo() != null) {
                batteryInfoListeners.pushData(currentDeviceId, newDeviceData.getBatteryInfo());
                batteryInfoUnfilteredListeners.pushData(currentDeviceId, newDeviceData.getBatteryInfo());
            }

            if (newDeviceData.getShiftingInfo() != null) {
                shiftingInfoListeners.pushData(currentDeviceId, newDeviceData.getShiftingInfo());
            }

            if (newDeviceData.getPreferences() != null) {
                devicePreferencesListener.pushData(currentDeviceId, newDeviceData.getPreferences());
            }
        }
    }

    private boolean tryUpdateCurrentDevice() {
        List<DeviceData> sortedDeviceData = deviceDataMap.values().stream()
                .filter(deviceData -> {
                    DevicePreferencesView preferences = deviceData.getPreferences();
                    return preferences != null && preferences.isEnabled(context) && !preferences.isSwitchEventsOnly(context);
                }).sorted((a, b) -> {
                    DevicePreferencesView preferencesA = a.getPreferences();
                    DevicePreferencesView preferencesB = b.getPreferences();

                    assert preferencesA != null;
                    assert preferencesB != null;

                    int priority = preferencesA.getPriority(context) - preferencesB.getPriority(context);
                    if (priority != 0) {
                        return priority;
                    }

                    return preferencesA.getName(context).compareToIgnoreCase(preferencesB.getName(context));
                }).collect(Collectors.toList());

        for (DeviceData deviceData : sortedDeviceData) {
            ConnectionInfo connectionInfo = deviceData.getConnectionInfo();
            if (connectionInfo != null && connectionInfo.getConnectionStatus() == ConnectionStatus.ESTABLISHED) {
                if (currentDeviceId != deviceData.getDeviceId()) {
                    currentDeviceId = deviceData.getDeviceId();
                    return true;
                }

                return false;
            }
        }

        DeviceData currentDeviceData = deviceDataMap.get(currentDeviceId);
        if (currentDeviceData != null) {
            ConnectionInfo currentDeviceConnectionInfo = currentDeviceData.getConnectionInfo();
            if (currentDeviceConnectionInfo != null &&
                    currentDeviceConnectionInfo.getConnectionStatus() == ConnectionStatus.CONNECTING) {
                return false;
            }
        }

        for (DeviceData deviceData : sortedDeviceData) {
            if (currentDeviceId != deviceData.getDeviceId()) {
                currentDeviceId = deviceData.getDeviceId();
                return true;
            }

            return false;
        }

        return false;
    }

    public void onConnectionInfo(DeviceId deviceId, ConnectionInfo connectionInfo) {
        DeviceData deviceData = deviceDataMap.computeIfAbsent(deviceId, DeviceData::new);
        deviceData.setConnectionInfo(connectionInfo);

        if (Objects.equals(deviceId, currentDeviceId)) {
            connectionInfoListeners.pushData(deviceId, connectionInfo);
        }

        attemptToUpdateCurrentDevice();
    }

    public void onBattery(DeviceId deviceId, BatteryInfo batteryInfo) {
        DeviceData deviceData = deviceDataMap.computeIfAbsent(deviceId, DeviceData::new);
        deviceData.setBatteryInfo(batteryInfo);

        if (Objects.equals(deviceId, currentDeviceId)) {
            batteryInfoListeners.pushData(deviceId, batteryInfo);
        }

        batteryInfoUnfilteredListeners.pushData(deviceId, batteryInfo);
    }

    public void onShifting(DeviceId deviceId, ShiftingInfo shiftingInfo) {
        DeviceData deviceData = deviceDataMap.computeIfAbsent(deviceId, DeviceData::new);
        deviceData.setShiftingInfo(shiftingInfo);

        if (Objects.equals(deviceId, currentDeviceId)) {
            shiftingInfoListeners.pushData(deviceId, shiftingInfo);
        }
    }

    public void onDevicePreferences(DeviceId deviceId, DevicePreferencesView preferences) {
        DeviceData deviceData = deviceDataMap.computeIfAbsent(deviceId, DeviceData::new);
        deviceData.setPreferences(preferences);

        if (Objects.equals(deviceId, currentDeviceId)) {
            devicePreferencesListener.pushData(deviceId, preferences);
        }

        attemptToUpdateCurrentDevice();
    }

}
