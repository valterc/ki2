package com.valterc.ki2.fragments.devices.list;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.valterc.ki2.R;
import com.valterc.ki2.data.connection.ConnectionDataInfo;
import com.valterc.ki2.data.device.DeviceId;
import com.valterc.ki2.data.device.DeviceType;
import com.valterc.ki2.data.preferences.device.DevicePreferences;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

public class ListDevicesAdapter extends RecyclerView.Adapter<ListDevicesViewHolder> {

    private final Context context;
    private final Consumer<DeviceId> listenerConfigureDevice;
    private final Consumer<DeviceId> listenerReconnectDevice;
    private final List<DeviceId> devices;
    private final Map<DeviceId, DevicePreferences> devicePreferencesMap;
    private final Map<DeviceId, ConnectionDataInfo> connectionDataInfoMap;

    public ListDevicesAdapter(Context context, Consumer<DeviceId> listenerConfigureDevice, Consumer<DeviceId> listenerReconnectDevice) {
        this.context = context;
        this.listenerConfigureDevice = listenerConfigureDevice;
        this.listenerReconnectDevice = listenerReconnectDevice;
        this.devices = new ArrayList<>();
        this.devicePreferencesMap = new HashMap<>();
        this.connectionDataInfoMap = new HashMap<>();
    }

    @SuppressLint("NotifyDataSetChanged")
    public void setDevices(Collection<DeviceId> devices) {
        this.devices.clear();
        this.connectionDataInfoMap.clear();
        this.devicePreferencesMap.clear();

        if (devices != null && devices.size() > 0) {
            this.devices.addAll(devices);
        }

        for (DeviceId device: this.devices) {
            devicePreferencesMap.put(device, new DevicePreferences(context, device));
        }

        notifyDataSetChanged();
    }

    public void addConnectionDataInfo(ConnectionDataInfo connectionDataInfo) {
        int index = devices.indexOf(connectionDataInfo.getDeviceId());
        ConnectionDataInfo existingConnectionDataInfo = connectionDataInfoMap.get(connectionDataInfo.getDeviceId());
        if (index != -1 &&
                (existingConnectionDataInfo == null || existingConnectionDataInfo.getConnectionStatus() != connectionDataInfo.getConnectionStatus())) {
            this.connectionDataInfoMap.put(connectionDataInfo.getDeviceId(), connectionDataInfo);
            notifyItemChanged(index);
        }
    }

    @NonNull
    @Override
    public ListDevicesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.view_list_devices_item, parent, false);
        return new ListDevicesViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ListDevicesViewHolder holder, int position) {
        DeviceId deviceId = devices.get(position);

        if (deviceId.getDeviceType() == DeviceType.SHIMANO_SHIFTING) {
            holder.getImageViewIcon().setImageResource(R.drawable.ic_di2);
            holder.getTextViewName().setText(Objects.requireNonNull(devicePreferencesMap.get(deviceId)).getName());
        } else {
            holder.getImageViewIcon().setImageResource(R.drawable.ic_memory);
            String deviceLabel = holder.getTextViewName().getContext().getString(R.string.text_param_sensor_name, deviceId.getName());
            holder.getTextViewName().setText(deviceLabel);
        }

        setConnectionStatusIndicator(holder, connectionDataInfoMap.get(deviceId));
        holder.getRootView().setOnClickListener(e -> listenerConfigureDevice.accept(deviceId));
        holder.getButtonReconnect().setOnClickListener(e -> {
            listenerReconnectDevice.accept(deviceId);

            holder.getTextViewConnectionStatus().setText(R.string.text_connecting);
            holder.getTextViewConnectionStatus().setTextColor(holder.getTextViewConnectionStatus().getContext().getColor(R.color.hh_faded_gray));
            holder.getButtonReconnect().setVisibility(View.INVISIBLE);
        });
    }

    @Override
    public int getItemCount() {
        return devices.size();
    }

    private void setConnectionStatusIndicator(ListDevicesViewHolder holder, ConnectionDataInfo connectionDataInfo) {

        Context context = holder.getTextViewConnectionStatus().getContext();

        if (connectionDataInfo == null) {
            holder.getTextViewConnectionStatus().setText(R.string.text_waiting_for_state);
            holder.getTextViewConnectionStatus().setTextColor(context.getColor(R.color.hh_faded_gray));
            holder.getTextViewName().setTextColor(context.getColor(R.color.hh_faded_gray));
            holder.getButtonReconnect().setVisibility(View.INVISIBLE);
            return;
        }

        holder.getImageViewIcon().setImageTintList(ColorStateList.valueOf(context.getColor(R.color.hh_black)));
        holder.getTextViewName().setTextColor(context.getColor(R.color.hh_black));
        holder.getTextViewConnectionStatus().setVisibility(View.VISIBLE);

        switch (connectionDataInfo.getConnectionStatus()) {

            case INVALID:
                holder.getTextViewConnectionStatus().setText(R.string.text_failed);
                holder.getTextViewConnectionStatus().setTextColor(context.getColor(R.color.hh_red));
                holder.getButtonReconnect().setVisibility(View.VISIBLE);
                break;

            case NEW:
            case CONNECTING:
                holder.getTextViewConnectionStatus().setText(R.string.text_connecting);
                holder.getTextViewConnectionStatus().setTextColor(context.getColor(R.color.hh_faded_gray));
                holder.getButtonReconnect().setVisibility(View.INVISIBLE);
                break;

            case ESTABLISHED:
                holder.getTextViewConnectionStatus().setText(R.string.text_connected);
                holder.getTextViewConnectionStatus().setTextColor(context.getColor(R.color.hh_green));
                holder.getButtonReconnect().setVisibility(View.INVISIBLE);
                break;

            case CLOSED:
                holder.getTextViewConnectionStatus().setText(R.string.text_not_found);
                holder.getTextViewConnectionStatus().setTextColor(context.getColor(R.color.hh_red));
                holder.getButtonReconnect().setVisibility(View.VISIBLE);
                break;

        }

    }

}
