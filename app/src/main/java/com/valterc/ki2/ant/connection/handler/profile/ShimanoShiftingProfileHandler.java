package com.valterc.ki2.ant.connection.handler.profile;

import android.os.Parcelable;

import com.dsi.ant.message.MessageUtils;
import com.dsi.ant.message.Rssi;
import com.dsi.ant.message.fromant.AcknowledgedDataMessage;
import com.dsi.ant.message.fromant.BroadcastDataMessage;
import com.valterc.ki2.ant.connection.IDeviceConnectionListener;
import com.valterc.ki2.ant.connection.handler.transport.ITransportHandler;
import com.valterc.ki2.data.command.CommandType;
import com.valterc.ki2.data.device.BatteryInfo;
import com.valterc.ki2.data.device.DeviceId;
import com.valterc.ki2.data.device.ShimanoPageType;
import com.valterc.ki2.data.device.SignalInfo;
import com.valterc.ki2.data.device.SlaveStatusIndicator;
import com.valterc.ki2.data.info.DataType;
import com.valterc.ki2.data.info.Manufacturer;
import com.valterc.ki2.data.info.ManufacturerInfoBuilder;
import com.valterc.ki2.data.shifting.BuzzerData;
import com.valterc.ki2.data.shifting.BuzzerType;
import com.valterc.ki2.data.shifting.FrontTeethPattern;
import com.valterc.ki2.data.shifting.RearTeethPattern;
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

@SuppressWarnings({"FieldCanBeLocal", "unused"})
public class ShimanoShiftingProfileHandler implements IDeviceProfileHandler {

    private static final Collection<CommandType> SUPPORTED_COMMANDS = Collections.singletonList(CommandType.SHIFTING_MODE);

    private final DeviceId deviceId;
    private final ITransportHandler transportHandler;
    private final IDeviceConnectionListener deviceConnectionListener;

    private final Set<SlaveStatusIndicator> missingIndicators;
    private final byte[] broadcastData;
    private int requestShiftModeTransitionSequenceNumber;
    private byte[] requestShiftModeTransitionData;

    private final ShiftingInfoBuilder shiftingInfoBuilder;
    private final ManufacturerInfoBuilder manufacturerInfoBuilder;
    private final SwitchData switchDataCH1;
    private final SwitchData switchDataCH2;
    private final SwitchData switchDataCH3;
    private final SwitchData switchDataCH4;
    private final BuzzerData buzzerData;


    public ShimanoShiftingProfileHandler(DeviceId deviceId, ITransportHandler transportHandler, IDeviceConnectionListener deviceConnectionListener) {
        this.deviceId = deviceId;
        this.transportHandler = transportHandler;
        this.deviceConnectionListener = deviceConnectionListener;
        this.missingIndicators = new HashSet<>(Arrays.asList(
                SlaveStatusIndicator.BATTERY_INDICATOR,
                SlaveStatusIndicator.FRONT_SPEEDS_CURRENT,
                SlaveStatusIndicator.REAR_SPEEDS_CURRENT,
                SlaveStatusIndicator.FRONT_SPEEDS_MAX,
                SlaveStatusIndicator.REAR_SPEEDS_MAX,
                SlaveStatusIndicator.SWITCH_COMMAND_NUMBER,
                SlaveStatusIndicator.CHAINRINGS));
        this.broadcastData = new byte[8];

        this.shiftingInfoBuilder = new ShiftingInfoBuilder();
        this.shiftingInfoBuilder.setBuzzerType(BuzzerType.DEFAULT);
        this.shiftingInfoBuilder.setFrontTeethPattern(FrontTeethPattern.UNKNOWN);
        this.shiftingInfoBuilder.setRearTeethPattern(RearTeethPattern.UNKNOWN);

        this.manufacturerInfoBuilder = new ManufacturerInfoBuilder();
        this.manufacturerInfoBuilder.setComponentId(null);

        this.switchDataCH1 = new SwitchData();
        this.switchDataCH2 = new SwitchData();
        this.switchDataCH3 = new SwitchData();
        this.switchDataCH4 = new SwitchData();
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

            case CHAINRINGS:
                handleChainringsPage(payload);
                break;

            default:
                Timber.d("[%s] Unhandled page %s: %s", deviceId, pageType, Arrays.toString(payload));
                break;
        }
    }

    private void handleChainringsPage(byte[] payload) {
        this.missingIndicators.remove(SlaveStatusIndicator.CHAINRINGS);

        int frontTeethPatternId = (int) MessageUtils.numberFromBytes(payload, 1, 1);
        int rearTeethPatternId = (int) MessageUtils.numberFromBytes(payload, 3, 1);

        FrontTeethPattern frontTeethPattern = FrontTeethPattern.fromId(frontTeethPatternId);
        RearTeethPattern rearTeethPattern = RearTeethPattern.fromId(rearTeethPatternId);

        Timber.d("[%s] Received chainrings: {front=%s, rear=%s}", deviceId, frontTeethPattern, rearTeethPattern);

        shiftingInfoBuilder.setFrontTeethPattern(frontTeethPattern);
        shiftingInfoBuilder.setRearTeethPattern(rearTeethPattern);
    }

    private void handleProductInformation(byte[] payload) {
        int minorRevision = (int) MessageUtils.numberFromBytes(payload, 2, 1);
        int majorRevision = (int) MessageUtils.numberFromBytes(payload, 3, 1);
        String softwareVersionFromRevisions = softwareVersionFromRevisions(majorRevision, minorRevision);
        long serialNumber = MessageUtils.numberFromBytes(payload, 4, 4);

        Timber.d("[%s] Received manufacturer info: {software=%s, serial=%s}", deviceId, softwareVersionFromRevisions, serialNumber);

        if (serialNumber != 0xFFFFFFFFL) {
            manufacturerInfoBuilder.setSerialNumber(Long.toString(serialNumber));
        } else {
            manufacturerInfoBuilder.setSerialNumber(null);
        }

        manufacturerInfoBuilder.setSoftwareVersion(softwareVersionFromRevisions);

        if (manufacturerInfoBuilder.allSet()) {
            deviceConnectionListener.onData(deviceId, DataType.MANUFACTURER_INFO, manufacturerInfoBuilder.build());
        }
    }

    private String softwareVersionFromRevisions(int majorRevision, int minorRevision) {
        if (minorRevision == 255) {
            return String.valueOf((float) majorRevision / 10);
        }

        return String.valueOf((float) ((majorRevision * 100) + minorRevision) / 1000);
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
        BuzzerType buzzerType = BuzzerType.fromCommandNumber((int) MessageUtils.numberFromBytes(payload, 1, 1));

        if (sequenceNumber != buzzerData.getSequenceNumber()) {
            Timber.d("[%s] Received buzzer data: {type=%s, sequence=%s}", deviceId, buzzerType, sequenceNumber);
            buzzerData.setSequenceNumber(sequenceNumber);
            shiftingInfoBuilder.setBuzzerType(buzzerType);
        }
    }

    private void handleSwitchStatusPage(byte[] payload) {
        this.missingIndicators.remove(SlaveStatusIndicator.SWITCH_COMMAND_NUMBER);

        int sequenceNumberCH2 = (int) (MessageUtils.numberFromBytes(payload, 1, 1) & 15);
        SwitchCommand switchCommandCH2 = SwitchCommand.fromCommandNumber((int) MessageUtils.numberFromBytes(payload, 1, 1) & 240);
        handleSwitch(switchCommandCH2, switchDataCH2, sequenceNumberCH2, SwitchType.D_FLY_CH2);

        int sequenceNumberCH1 = (int) (MessageUtils.numberFromBytes(payload, 2, 1) & 15);
        SwitchCommand switchCommandCH1 = SwitchCommand.fromCommandNumber((int) MessageUtils.numberFromBytes(payload, 2, 1) & 240);
        handleSwitch(switchCommandCH1, switchDataCH1, sequenceNumberCH1, SwitchType.D_FLY_CH1);

        int sequenceNumberCH4 = (int) (MessageUtils.numberFromBytes(payload, 3, 1) & 15);
        SwitchCommand switchCommandCH4 = SwitchCommand.fromCommandNumber((int) MessageUtils.numberFromBytes(payload, 3, 1) & 240);
        handleSwitch(switchCommandCH4, switchDataCH4, sequenceNumberCH4, SwitchType.D_FLY_CH4);

        int sequenceNumberCH3 = (int) (MessageUtils.numberFromBytes(payload, 4, 1) & 15);
        SwitchCommand switchCommandCH3 = SwitchCommand.fromCommandNumber((int) MessageUtils.numberFromBytes(payload, 4, 1) & 240);
        handleSwitch(switchCommandCH3, switchDataCH3, sequenceNumberCH3, SwitchType.D_FLY_CH3);

        Timber.d("[%s] Received switch info: {CH1={command=%s, sequence=%s}, CH2={command=%s, sequence=%s}, CH3={command=%s, sequence=%s}, CH4={command=%s, sequence=%s}}",
                deviceId,
                switchCommandCH1, sequenceNumberCH1,
                switchCommandCH2, sequenceNumberCH2,
                switchCommandCH3, sequenceNumberCH3,
                switchCommandCH4, sequenceNumberCH4);
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
        this.missingIndicators.remove(SlaveStatusIndicator.FRONT_SPEEDS_MAX);
        this.missingIndicators.remove(SlaveStatusIndicator.REAR_SPEEDS_MAX);

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

        if (shiftingInfoBuilder.allSet()) {
            deviceConnectionListener.onData(deviceId, DataType.SHIFTING, shiftingInfoBuilder.build());
        }
    }

    private void handleBatterySpeedsPage(byte[] payload) {
        this.missingIndicators.remove(SlaveStatusIndicator.BATTERY_INDICATOR);

        int frontGear = (int) MessageUtils.numberFromBytes(payload, 2, 1);
        int rearGear = (int) MessageUtils.numberFromBytes(payload, 3, 1);

        if ((frontGear != 255 && frontGear != 0) || (rearGear != 255 && rearGear != 0)) {
            this.missingIndicators.remove(SlaveStatusIndicator.FRONT_SPEEDS_CURRENT);
            this.missingIndicators.remove(SlaveStatusIndicator.REAR_SPEEDS_CURRENT);

            Timber.d("[%s] Received speeds: {front=%s, rear=%s}", deviceId, frontGear, rearGear);

            if (frontGear == 0 || frontGear == 255) {
                frontGear = 1;
            }

            if (rearGear == 0 || rearGear == 255) {
                rearGear = 1;
            }

            if (buzzerData.isExpired()) {
                Timber.d("[%s] Buzzer Data expired", deviceId);
                buzzerData.resetTime();
                shiftingInfoBuilder.setBuzzerType(BuzzerType.DEFAULT);
            }

            shiftingInfoBuilder.setFrontGear(frontGear);
            shiftingInfoBuilder.setRearGear(rearGear);
        }

        int batteryPercentage = (int) MessageUtils.numberFromBytes(payload, 4, 1);
        deviceConnectionListener.onData(deviceId, DataType.BATTERY, new BatteryInfo(batteryPercentage));

        ShiftingMode shiftingMode = ShiftingMode.fromValue((int) MessageUtils.numberFromBytes(payload, 5, 1));
        shiftingInfoBuilder.setShiftingMode(shiftingMode);

        if (shiftingInfoBuilder.allSet()) {
            deviceConnectionListener.onData(deviceId, DataType.SHIFTING, shiftingInfoBuilder.build());
        }
    }

    private byte[] encodeSlaveStatus(Collection<SlaveStatusIndicator> slaveStatusIndicatorCollection) {
        long bitSet = 0xFFFFFFFFL;

        for (SlaveStatusIndicator slaveStatusIndicator : slaveStatusIndicatorCollection) {
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
    public void sendCommand(CommandType commandType, Parcelable data) {
        switch (commandType) {
            case SHIFTING_MODE:
                changeShiftMode();
                break;

            case UNKNOWN:
            default:
                throw new RuntimeException("Unsupported command: " + commandType);
        }
    }

    private void changeShiftMode() {
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
