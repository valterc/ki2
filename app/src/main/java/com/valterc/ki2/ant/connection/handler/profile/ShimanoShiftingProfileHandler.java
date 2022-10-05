package com.valterc.ki2.ant.connection.handler.profile;

import com.dsi.ant.message.MessageUtils;
import com.dsi.ant.message.Rssi;
import com.dsi.ant.message.fromant.AcknowledgedDataMessage;
import com.dsi.ant.message.fromant.BroadcastDataMessage;
import com.valterc.ki2.ant.connection.IDeviceConnectionListener;
import com.valterc.ki2.ant.connection.handler.transport.ITransportHandler;
import com.valterc.ki2.data.command.CommandType;
import com.valterc.ki2.data.device.BatteryInfo;
import com.valterc.ki2.data.device.DeviceId;
import com.valterc.ki2.data.device.DeviceType;
import com.valterc.ki2.data.device.ShimanoPageType;
import com.valterc.ki2.data.device.SignalInfo;
import com.valterc.ki2.data.device.StatusIndicator;
import com.valterc.ki2.data.info.DataType;
import com.valterc.ki2.data.info.Manufacturer;
import com.valterc.ki2.data.info.ManufacturerInfoBuilder;
import com.valterc.ki2.data.shifting.BuzzerData;
import com.valterc.ki2.data.shifting.BuzzerPattern;
import com.valterc.ki2.data.shifting.ShiftingInfoBuilder;
import com.valterc.ki2.data.shifting.ShiftingMode;
import com.valterc.ki2.data.switches.SwitchCommand;
import com.valterc.ki2.data.switches.SwitchData;
import com.valterc.ki2.data.switches.SwitchEvent;
import com.valterc.ki2.data.switches.SwitchType;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import timber.log.Timber;

public class ShimanoShiftingProfileHandler implements IDeviceProfileHandler {

    private static final Collection<CommandType> SUPPORTED_COMMANDS = Collections.singletonList(CommandType.SHIFTING_MODE);

    private final DeviceId deviceId;
    private final ITransportHandler transportHandler;
    private final IDeviceConnectionListener deviceConnectionListener;

    private final Set<StatusIndicator> missingIndicators;
    private final byte[] broadcastData;
    private int requestShiftModeTransitionSequenceNumber;
    private byte[] requestShiftModeTransitionData;

    private final ShiftingInfoBuilder shiftingInfoBuilder;
    private final ManufacturerInfoBuilder manufacturerInfoBuilder;
    private final SwitchData switchDataLeft;
    private final SwitchData switchDataRight;
    private final BuzzerData buzzerData;

    public ShimanoShiftingProfileHandler(DeviceId deviceId, ITransportHandler transportHandler, IDeviceConnectionListener deviceConnectionListener) {

        if (deviceId.getDeviceType() != DeviceType.SHIMANO_SHIFTING) {
            throw new RuntimeException("Invalid profile handler for device type " + deviceId.getDeviceType());
        }

        this.deviceId = deviceId;
        this.transportHandler = transportHandler;
        this.deviceConnectionListener = deviceConnectionListener;
        this.missingIndicators = new HashSet<>(Arrays.asList(
                StatusIndicator.BATTERY_INDICATOR,
                StatusIndicator.FRONT_SPEEDS_CURRENT,
                StatusIndicator.REAR_SPEEDS_CURRENT,
                StatusIndicator.FRONT_SPEEDS_MAX,
                StatusIndicator.REAR_SPEEDS_MAX,
                StatusIndicator.SWITCH_COMMAND_NUMBER));
        this.broadcastData = new byte[8];

        this.shiftingInfoBuilder = new ShiftingInfoBuilder();
        this.shiftingInfoBuilder.setBuzzerOn(false);
        this.manufacturerInfoBuilder = new ManufacturerInfoBuilder();
        this.manufacturerInfoBuilder.setComponentId(null);

        this.switchDataLeft = new SwitchData();
        this.switchDataRight = new SwitchData();
        this.buzzerData = new BuzzerData();
    }

    private void handleData(byte[] payload) {

        ShimanoPageType pageType = ShimanoPageType.fromPageNumber(payload[0]);

        switch (pageType) {

            case BATTERY_LEVEL_AND_NUMBER_OF_SPEEDS:
                handleBatterySpeedsPage(payload);
                break;

            case BIKE_STATUS:
                handleBikeStatusPage(payload);
                break;

            case SWITCH_STATUS:
                handleSwitchStatusPage(payload);
                break;

            case BUZZER_NOTIFICATION:
                handleBuzzerNotificationPage(payload);
                break;

            case SHIFT_MODE_TRANSITION_ACK:
                this.requestShiftModeTransitionData = null;
                Timber.i("[%s] Shift mode transition acknowledged", deviceId);
                break;

            case MANUFACTURER_INFORMATION:
                handleManufacturerInformation(payload);
                break;

            case PRODUCT_INFORMATION:
                handleProductInformation(payload);
                break;

            default:
                Timber.d("[%s] Unhandled page %s", deviceId, pageType);
                break;
        }
    }

    private void handleProductInformation(byte[] payload) {
        int minorRevision = (int) MessageUtils.numberFromBytes(payload, 2, 1);
        int majorRevision = (int) MessageUtils.numberFromBytes(payload, 3, 1);
        String softwareVersionFromRevisions = softwareVersionFromRevisions(majorRevision, minorRevision);
        long serialNumber = MessageUtils.numberFromBytes(payload, 4, 4);

        Timber.d("[%s] Received manufacturer info: {software=%s, serial=%s}", deviceId, softwareVersionFromRevisions, serialNumber);

        if (serialNumber != 0xFFFFFFFF) {
            manufacturerInfoBuilder.setSerialNumber(Long.toString(serialNumber));
        }else {
            manufacturerInfoBuilder.setSerialNumber(null);
        }

        manufacturerInfoBuilder.setSoftwareVersion(softwareVersionFromRevisions);

        if (manufacturerInfoBuilder.allSet()) {
            deviceConnectionListener.onData(deviceId, DataType.MANUFACTURER_INFO, manufacturerInfoBuilder.build());
        }
    }

    public final String softwareVersionFromRevisions(int majorRevision, int minorRevision) {
        if (minorRevision == 255) {
            return String.valueOf((float)majorRevision / 10);
        }

        return String.valueOf((float)((majorRevision * 100) + minorRevision) / 1000);
    }


    private void handleManufacturerInformation(byte[] payload) {
        String hardwareVersion = String.valueOf(MessageUtils.numberFromBytes(payload, 3, 1));
        int manufacturerId = (int) MessageUtils.numberFromBytes(payload, 4, 2);
        String modelNumber = String.valueOf(MessageUtils.numberFromBytes(payload, 6, 2));

        Timber.d("[%s] Received manufacturer info: {hardware=%s, manufacturerId=%s, model=%s}", deviceId, hardwareVersion, manufacturerId, modelNumber);

        manufacturerInfoBuilder.setHardwareVersion(hardwareVersion);
        manufacturerInfoBuilder.setManufacturer(Manufacturer.fromId(manufacturerId));
        manufacturerInfoBuilder.setModelNumber(modelNumber);

        if (manufacturerInfoBuilder.allSet()) {
            deviceConnectionListener.onData(deviceId, DataType.MANUFACTURER_INFO, manufacturerInfoBuilder.build());
        }
    }

    private void handleBuzzerNotificationPage(byte[] payload) {
        int sequenceNumber = (int) (MessageUtils.numberFromBytes(payload, 2, 1) & 15);
        BuzzerPattern buzzerPattern = BuzzerPattern.fromCommandNumber((int)MessageUtils.numberFromBytes(payload, 1, 1));

        if (sequenceNumber != buzzerData.getSequenceNumber()) {
            buzzerData.setSequenceNumber(sequenceNumber);
            boolean buzzerOn = buzzerPattern == BuzzerPattern.OVERLIMIT_PROTECTION;

            buzzerData.setTime(buzzerOn ? System.currentTimeMillis() : 0L);
            shiftingInfoBuilder.setBuzzerOn(buzzerOn);

            if (shiftingInfoBuilder.allSet()){
                deviceConnectionListener.onData(deviceId, DataType.SHIFTING, shiftingInfoBuilder.build());
            }
        }
    }

    private void handleSwitchStatusPage(byte[] payload) {
        this.missingIndicators.remove(StatusIndicator.SWITCH_COMMAND_NUMBER);

        int sequenceNumberRight = (int) (MessageUtils.numberFromBytes(payload, 1, 1) & 15);
        SwitchCommand switchCommandRight = SwitchCommand.fromCommandNumber((int)MessageUtils.numberFromBytes(payload, 1, 1) & 240);
        handleSwitch(switchCommandRight, switchDataRight, sequenceNumberRight, SwitchType.RIGHT);

        int sequenceNumberLeft = (int) (MessageUtils.numberFromBytes(payload, 2, 1) & 15);
        SwitchCommand switchCommandLeft = SwitchCommand.fromCommandNumber((int)MessageUtils.numberFromBytes(payload, 2, 1) & 240);
        handleSwitch(switchCommandLeft, switchDataLeft, sequenceNumberLeft, SwitchType.LEFT);

        Timber.d("[%s] Received switch info: {left={command=%s, sequence=%s}, right={command=%s, sequence=%s}}",
                deviceId,
                switchCommandLeft, sequenceNumberLeft,
                switchCommandRight, sequenceNumberRight);
    }

    private void handleSwitch(SwitchCommand switchCommand, SwitchData switchData, int sequenceNumber, SwitchType type) {
        if (switchCommand != SwitchCommand.NO_SWITCH
                && switchData.getSequenceNumber() != -1
                && switchData.getSequenceNumber() != sequenceNumber) {

            if (switchCommand == SwitchCommand.LONG_PRESS_CONTINUE) {
                switchData.incrementRepeat();
            } else {
                switchData.resetRepeat();
            }

            deviceConnectionListener.onData(deviceId, DataType.SWITCH, new SwitchEvent(type, switchCommand, switchData.getRepeat()));
        }
        switchData.setSequenceNumber(sequenceNumber);
    }

    private void handleBikeStatusPage(byte[] payload) {
        this.missingIndicators.remove(StatusIndicator.FRONT_SPEEDS_MAX);
        this.missingIndicators.remove(StatusIndicator.REAR_SPEEDS_MAX);

        int frontGearMax = (int) MessageUtils.numberFromBytes(payload, 2, 1);
        int rearGearMax = (int) MessageUtils.numberFromBytes(payload, 3, 1);

        Timber.d("[%s] Received bike status: {front_gear_max=%s, rear_gear_max=%s}", deviceId, frontGearMax, rearGearMax);

        if (frontGearMax == 0 || frontGearMax == 255) {
            frontGearMax = 1;
        }

        if (rearGearMax == 0 || rearGearMax == 255) {
            rearGearMax = 1;
        }

        shiftingInfoBuilder.setFrontGearMax(frontGearMax);
        shiftingInfoBuilder.setRearGearMax(rearGearMax);

        if (shiftingInfoBuilder.allSet()){
            deviceConnectionListener.onData(deviceId, DataType.SHIFTING, shiftingInfoBuilder.build());
        }
    }

    private void handleBatterySpeedsPage(byte[] payload) {
        this.missingIndicators.remove(StatusIndicator.BATTERY_INDICATOR);

        int frontGear = (int) MessageUtils.numberFromBytes(payload, 2, 1);
        int rearGear = (int) MessageUtils.numberFromBytes(payload, 3, 1);

        if ((frontGear != 255 && frontGear != 0) || (rearGear != 255 && rearGear != 0)) {
            this.missingIndicators.remove(StatusIndicator.FRONT_SPEEDS_CURRENT);
            this.missingIndicators.remove(StatusIndicator.REAR_SPEEDS_CURRENT);

            Timber.d("[%s] Received speeds: {front=%s, rear=%s}", deviceId, frontGear, rearGear);

            if (frontGear == 0 || frontGear == 255) {
                frontGear = 1;
            }

            if (rearGear == 0 || rearGear == 255) {
                rearGear = 1;
            }

            if (buzzerData.isExpired())
            {
                buzzerData.resetTime();
                shiftingInfoBuilder.setBuzzerOn(false);
            }

            shiftingInfoBuilder.setFrontGear(frontGear);
            shiftingInfoBuilder.setRearGear(rearGear);
        }

        int batteryPercentage = (int) MessageUtils.numberFromBytes(payload, 4, 1);
        deviceConnectionListener.onData(deviceId, DataType.BATTERY, new BatteryInfo(batteryPercentage));

        Timber.d("[%s] Received battery: %s", deviceId, batteryPercentage);

        ShiftingMode shiftingMode = ShiftingMode.fromValue((int)MessageUtils.numberFromBytes(payload, 5, 1));
        shiftingInfoBuilder.setShiftingMode(shiftingMode);

        Timber.d("[%s] Received shifting mode: %s", deviceId, shiftingMode);

        if (shiftingInfoBuilder.allSet()) {
            deviceConnectionListener.onData(deviceId, DataType.SHIFTING, shiftingInfoBuilder.build());
        }
    }

    private byte[] encodeSlaveStatus(Collection<StatusIndicator> statusIndicatorCollection) {
        long bitSet = 0xFFFFFFFF;

        for (StatusIndicator slaveStatusIndicator : statusIndicatorCollection) {
            bitSet &= ~slaveStatusIndicator.getFlag();
        }

        broadcastData[0] = (byte) ShimanoPageType.ANT_SLAVE_STATUS.getPageNumber();
        broadcastData[1] = (byte) bitSet;
        broadcastData[2] = (byte) (bitSet >> 8);
        broadcastData[3] = (byte) (bitSet >> 16);
        broadcastData[4] = (byte) (bitSet >> 24);
        broadcastData[5] = (byte) 255;
        broadcastData[6] = (byte) 255;
        broadcastData[7] = (byte) 255;

        return broadcastData;

    }

    @Override
    public byte[] getAcknowledgedData() {
        return null;
    }

    @Override
    public byte[] getBroadcastData() {
        byte[] data = this.requestShiftModeTransitionData;
        if (data != null) {
            Timber.d("[%s] Requesting shift mode transition (sequence number: %s)", deviceId, requestShiftModeTransitionSequenceNumber);
            return data;
        }

        if (!this.missingIndicators.isEmpty()) {
            Timber.d("[%s] Requesting indicators: %s", deviceId, missingIndicators);
        }

        return encodeSlaveStatus(missingIndicators);
    }

    @Override
    public void onRssi(Rssi rssi) {
        deviceConnectionListener.onData(deviceId, DataType.SIGNAL, new SignalInfo(rssi.getRssiValue()));
    }

    @Override
    public void onAcknowledgedData(AcknowledgedDataMessage acknowledgedDataMessage) {
        handleData(acknowledgedDataMessage.getPayload());
    }

    @Override
    public void onBroadcastData(BroadcastDataMessage broadcastDataMessage) {
        handleData(broadcastDataMessage.getPayload());
    }

    @Override
    public Collection<CommandType> getSupportedCommands() {
        return SUPPORTED_COMMANDS;
    }

    @Override
    public void sendCommand(CommandType commandType, Object data) {
        switch (commandType) {

            case SHIFTING_MODE:
                changeShiftMode();
                break;

            case UNKNOWN:
            default:
                throw new RuntimeException("Unsupported command: " + commandType);
        }
    }

    public void changeShiftMode() {
        byte[] data = new byte[8];
        this.requestShiftModeTransitionSequenceNumber++;
        data[0] = (byte) ShimanoPageType.REQUEST_SHIFT_MODE_TRANSITION.getPageNumber();
        data[1] = (byte) ((this.requestShiftModeTransitionSequenceNumber & 15) | 224);
        data[2] = (byte) 255;
        data[3] = (byte) 255;
        data[4] = (byte) 255;
        data[5] = (byte) 255;
        data[6] = (byte) 255;
        data[7] = (byte) 255;
        this.requestShiftModeTransitionData = data;
    }

}
