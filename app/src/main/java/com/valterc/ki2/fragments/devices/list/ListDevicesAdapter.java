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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class ListDevicesAdapter extends RecyclerView.Adapter<ListDevicesViewHolder> {

    private final Consumer<DeviceId> listenerConfigureDevice;
    private final Consumer<DeviceId> listenerRemoveDevice;
    private final List<DeviceId> devices;
    private final Map<DeviceId, ConnectionDataInfo> connectionDataInfoMap;

    public ListDevicesAdapter(Consumer<DeviceId> listenerConfigureDevice, Consumer<DeviceId> listenerRemoveDevice) {
        this.listenerConfigureDevice = listenerConfigureDevice;
        this.listenerRemoveDevice = listenerRemoveDevice;
        this.devices = new ArrayList<>();
        this.connectionDataInfoMap = new HashMap<>();
    }

    @SuppressLint("NotifyDataSetChanged")
    public void setDevices(Collection<DeviceId> devices) {
        this.devices.clear();
        this.connectionDataInfoMap.clear();

        if (devices != null && devices.size() > 0) {
            this.devices.addAll(devices);
        }

        notifyDataSetChanged();
    }

    public void addConnectionDataInfo(ConnectionDataInfo connectionDataInfo) {
        int index = devices.indexOf(connectionDataInfo.getDeviceId());
        if (index != -1) {
            this.connectionDataInfoMap.put(connectionDataInfo.getDeviceId(), connectionDataInfo);
            notifyItemChanged(index);
        }
    }

    public void onDeviceRemoved(DeviceId deviceId) {
        int index = devices.indexOf(deviceId);
        if (index != -1) {
            devices.remove(deviceId);
            connectionDataInfoMap.remove(deviceId);
            notifyItemRemoved(index);
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
        String deviceName = (deviceId.getAntDeviceId() != null ? deviceId.getAntDeviceId().toString() : deviceId.getUid());

        if (deviceId.getDeviceType() == DeviceType.SHIMANO_SHIFTING) {
            holder.getImageViewIcon().setImageResource(R.drawable.ic_di2);
            String deviceLabel = holder.getTextViewName().getContext().getString(R.string.text_di2_name, deviceName);
            holder.getTextViewName().setText(deviceLabel);
        } else {
            holder.getImageViewIcon().setImageResource(R.drawable.ic_memory);
            String deviceLabel = holder.getTextViewName().getContext().getString(R.string.text_sensor_name, deviceName);
            holder.getTextViewName().setText(deviceLabel);
        }

        setConnectionStatusIndicator(holder, connectionDataInfoMap.get(deviceId));
        holder.getRootView().setOnClickListener(e -> listenerConfigureDevice.accept(deviceId));
        holder.getButtonConfigure().setOnClickListener(e -> listenerConfigureDevice.accept(deviceId));
        holder.getButtonRemove().setOnClickListener(e -> listenerRemoveDevice.accept(deviceId));
    }

    @Override
    public int getItemCount() {
        return devices.size();
    }

    private void setConnectionStatusIndicator(ListDevicesViewHolder holder, ConnectionDataInfo connectionDataInfo) {

        Context context = holder.getTextViewConnectionStatus().getContext();

        if (connectionDataInfo == null) {
            holder.getTextViewConnectionStatus().setVisibility(View.INVISIBLE);
            holder.getImageViewIcon().setImageTintList(ColorStateList.valueOf(context.getColor(R.color.hh_faded_gray)));
            holder.getTextViewName().setTextColor(context.getColor(R.color.hh_faded_gray));
            return;
        }

        holder.getImageViewIcon().setImageTintList(ColorStateList.valueOf(context.getColor(R.color.hh_black)));
        holder.getTextViewName().setTextColor(context.getColor(R.color.hh_black));
        holder.getTextViewConnectionStatus().setVisibility(View.VISIBLE);

        switch (connectionDataInfo.getConnectionStatus()) {

            case INVALID:
                holder.getTextViewConnectionStatus().setText(R.string.text_failed);
                holder.getTextViewConnectionStatus().setTextColor(context.getColor(R.color.hh_red));
                break;

            case NEW:
            case CONNECTING:
                holder.getTextViewConnectionStatus().setText(R.string.text_connecting);
                holder.getTextViewConnectionStatus().setTextColor(context.getColor(R.color.hh_faded_gray));
                break;

            case ESTABLISHED:
                holder.getTextViewConnectionStatus().setText(R.string.text_connected);
                holder.getTextViewConnectionStatus().setTextColor(context.getColor(R.color.hh_green));
                break;

            case CLOSED:
                holder.getTextViewConnectionStatus().setText(R.string.text_not_found);
                holder.getTextViewConnectionStatus().setTextColor(context.getColor(R.color.hh_red_700));
                break;

        }

    }

}
