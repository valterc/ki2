package com.valterc.ki2.fragments.devices.list;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.valterc.ki2.R;
import com.valterc.ki2.data.device.DeviceId;
import com.valterc.ki2.data.device.DeviceType;
import com.valterc.ki2.fragments.devices.add.SearchDevicesViewHolder;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;

public class ListDevicesAdapter extends RecyclerView.Adapter<ListDevicesViewHolder> {

    private final Consumer<DeviceId> listenerConfigureDevice;
    private final Consumer<DeviceId> listenerRemoveDevice;
    private final List<DeviceId> devices;

    public ListDevicesAdapter(Consumer<DeviceId> listenerConfigureDevice, Consumer<DeviceId> listenerRemoveDevice) {
        this.listenerConfigureDevice = listenerConfigureDevice;
        this.listenerRemoveDevice = listenerRemoveDevice;
        this.devices = new ArrayList<>();
    }

    @SuppressLint("NotifyDataSetChanged")
    public void setDevices(Collection<DeviceId> devices)
    {
        if (devices == null || devices.size() == 0){
            this.devices.clear();
        } else {
            this.devices.clear();
            this.devices.addAll(devices);
        }

        notifyDataSetChanged();
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

        holder.getTextViewConnectionStatus().setText(R.string.text_connecting);
        holder.getRootView().setOnClickListener(e -> listenerConfigureDevice.accept(deviceId));
        holder.getButtonConfigure().setOnClickListener(e -> listenerConfigureDevice.accept(deviceId));
        holder.getButtonRemove().setOnClickListener(e -> listenerRemoveDevice.accept(deviceId));
    }

    @Override
    public int getItemCount() {
        return devices.size();
    }

}
