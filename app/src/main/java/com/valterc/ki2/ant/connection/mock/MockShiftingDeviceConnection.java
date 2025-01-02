package com.valterc.ki2.ant.connection.mock;

import android.os.Handler;
import android.os.Looper;
import android.os.Parcelable;

import com.valterc.ki2.ant.connection.IAntDeviceConnection;
import com.valterc.ki2.ant.connection.IDeviceConnectionListener;
import com.valterc.ki2.data.command.CommandType;
import com.valterc.ki2.data.connection.ConnectionStatus;
import com.valterc.ki2.data.device.BatteryInfo;
import com.valterc.ki2.data.device.DeviceId;
import com.valterc.ki2.data.device.DeviceType;
import com.valterc.ki2.data.device.SignalInfo;
import com.valterc.ki2.data.info.DataType;
import com.valterc.ki2.data.info.Manufacturer;
import com.valterc.ki2.data.info.ManufacturerInfoBuilder;
import com.valterc.ki2.data.shifting.BuzzerType;
import com.valterc.ki2.data.shifting.FrontTeethPattern;
import com.valterc.ki2.data.shifting.RearTeethPattern;
import com.valterc.ki2.data.shifting.ShiftingInfoBuilder;
import com.valterc.ki2.data.shifting.ShiftingMode;

import java.util.Random;

public class MockShiftingDeviceConnection implements IAntDeviceConnection {

    private static final int TIME_MS_INIT_DISCONNECT = 15_000;
    private static final int TIME_MS_INIT_CONNECT = 7_000;
    private static final int TIME_MS_DATA_UPDATE = 500;
    private static final int VALUE_INIT_BATTERY_MIN = 20;
    private static final int VALUE_INIT_BATTERY_MAX = 70;
    private static final float PROBABILITY_INIT_DISCONNECT = .5f;
    private static final float PROBABILITY_DATA_REAR_SHIFT = .033f;
    private static final float PROBABILITY_DATA_FRONT_SHIFT = .025f;
    private static final float PROBABILITY_DATA_BATTERY_DECREASE = .001f;

    private final DeviceId deviceId;
    private final IDeviceConnectionListener deviceConnectionListener;

    private final Random random;

    private Looper looper;
    private Handler handler;

    private ConnectionStatus connectionStatus;
    private int battery;
    private ShiftingInfoBuilder shiftingInfoBuilder;
    private ManufacturerInfoBuilder manufacturerInfoBuilder;

    public MockShiftingDeviceConnection(DeviceId deviceId, IDeviceConnectionListener deviceConnectionListener) {
        if (deviceId.getDeviceType() != DeviceType.MOCK_SHIFTING) {
            throw new RuntimeException("Invalid connection for device type " + deviceId.getDeviceType());
        }

        this.deviceId = deviceId;
        this.deviceConnectionListener = deviceConnectionListener;
        this.random = new Random(deviceId.getDeviceNumber());
        initThread();
        initDevice();
    }

    private void initThread() {
        Thread thread = new Thread(() -> {
            Looper.prepare();
            this.looper = Looper.myLooper();
            this.handler = new Handler(this.looper);
            Looper.loop();
        }, "MockDeviceThread::" + deviceId.getUid());
        thread.start();

        while (this.handler == null) {
            Thread.yield();
        }
    }

    private void initDevice() {
        handler.post(() -> {
            setConnectionStatus(ConnectionStatus.CONNECTING);
            if (random.nextDouble() < PROBABILITY_INIT_DISCONNECT) {
                handler.postDelayed(this::disconnect, TIME_MS_INIT_DISCONNECT);
            } else {
                handler.postDelayed(() -> {
                    setConnectionStatus(ConnectionStatus.ESTABLISHED);

                    battery = VALUE_INIT_BATTERY_MIN + random.nextInt(VALUE_INIT_BATTERY_MAX - VALUE_INIT_BATTERY_MIN);

                    shiftingInfoBuilder = new ShiftingInfoBuilder();
                    shiftingInfoBuilder.setBuzzerType(BuzzerType.DEFAULT);
                    shiftingInfoBuilder.setFrontTeethPattern(FrontTeethPattern.P52_36);
                    shiftingInfoBuilder.setRearTeethPattern(RearTeethPattern.S12_P11_34);
                    shiftingInfoBuilder.setFrontGearMax(shiftingInfoBuilder.getFrontTeethPattern().getGearCount());
                    shiftingInfoBuilder.setRearGearMax(shiftingInfoBuilder.getRearTeethPattern().getGearCount());
                    shiftingInfoBuilder.setFrontGear(2);
                    shiftingInfoBuilder.setRearGear(1);
                    shiftingInfoBuilder.setShiftingMode(ShiftingMode.SYNCHRONIZED_SHIFT_MODE_2);

                    manufacturerInfoBuilder = new ManufacturerInfoBuilder();
                    manufacturerInfoBuilder.setComponentId(null);
                    manufacturerInfoBuilder.setManufacturer(Manufacturer.UNKNOWN);
                    manufacturerInfoBuilder.setHardwareVersion("1.0");
                    manufacturerInfoBuilder.setModelNumber("001");
                    manufacturerInfoBuilder.setSerialNumber("001");
                    manufacturerInfoBuilder.setSoftwareVersion("1.0");

                    deviceConnectionListener.onData(deviceId, DataType.BATTERY, new BatteryInfo(battery));
                    deviceConnectionListener.onData(deviceId, DataType.MANUFACTURER_INFO, manufacturerInfoBuilder.build());
                    handler.postDelayed(this::runDataFlow, TIME_MS_INIT_CONNECT);
                }, (long) (TIME_MS_INIT_CONNECT + TIME_MS_INIT_CONNECT * random.nextDouble()));
            }
        });
    }

    private void runDataFlow() {
        if (random.nextDouble() < PROBABILITY_DATA_REAR_SHIFT) {
            int shiftDirection = random.nextBoolean() ? 1 : -1;
            int targetGear = shiftingInfoBuilder.getRearGear() + shiftDirection;
            if (targetGear >= 1 && targetGear <= shiftingInfoBuilder.getRearGearMax()) {
                shiftingInfoBuilder.setRearGear(targetGear);
            }
        }

        if (random.nextDouble() < PROBABILITY_DATA_FRONT_SHIFT) {
            int shiftDirection = random.nextBoolean() ? 1 : -1;
            int targetGear = shiftingInfoBuilder.getFrontGear() + shiftDirection;
            if (targetGear >= 1 && targetGear <= shiftingInfoBuilder.getFrontGearMax()) {
                shiftingInfoBuilder.setFrontGear(targetGear);
            }
        }

        if (random.nextDouble() < PROBABILITY_DATA_BATTERY_DECREASE) {
            battery = Math.max(battery - 1, 0);
        }

        deviceConnectionListener.onData(deviceId, DataType.SHIFTING, shiftingInfoBuilder.build());
        deviceConnectionListener.onData(deviceId, DataType.BATTERY, new BatteryInfo(battery));
        deviceConnectionListener.onData(deviceId, DataType.SIGNAL, new SignalInfo(-1 * (30 + random.nextInt(60))));

        if (battery == 0) {
            disconnect();
        } else {
            handler.postDelayed(this::runDataFlow, TIME_MS_DATA_UPDATE);
        }
    }

    @Override
    public DeviceId getDeviceId() {
        return deviceId;
    }

    @Override
    public void disconnect() {
        this.looper.quit();
        setConnectionStatus(ConnectionStatus.CLOSED);
    }

    @Override
    public ConnectionStatus getConnectionStatus() {
        return connectionStatus;
    }

    @Override
    public void sendCommand(CommandType commandType, Parcelable data) {
        if (commandType == CommandType.SHIFTING_MODE) {
            changeShiftingMode();
        }
    }

    private void setConnectionStatus(ConnectionStatus connectionStatus) {
        this.connectionStatus = connectionStatus;
        deviceConnectionListener.onConnectionStatus(deviceId, connectionStatus);
    }

    private void changeShiftingMode() {
        if (connectionStatus != ConnectionStatus.ESTABLISHED) {
            return;
        }

        switch (shiftingInfoBuilder.getShiftingMode()) {
            case NORMAL:
                shiftingInfoBuilder.setShiftingMode(ShiftingMode.SYNCHRONIZED_SHIFT_MODE_1);
                break;
            case SYNCHRONIZED_SHIFT_MODE_1:
                shiftingInfoBuilder.setShiftingMode(ShiftingMode.SYNCHRONIZED_SHIFT_MODE_2);
                break;
            case SYNCHRONIZED_SHIFT_MODE_2:
                shiftingInfoBuilder.setShiftingMode(ShiftingMode.NORMAL);
                break;
        }
    }

}
