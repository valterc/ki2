package com.valterc.ki2.fragments.devices.add;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.valterc.ki2.R;
import com.valterc.ki2.data.device.DeviceId;
import com.valterc.ki2.data.device.DeviceType;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;

public class SearchDevicesAdapter extends RecyclerView.Adapter<SearchDevicesViewHolder> {

    private final Consumer<DeviceId> listenerAddDevice;
    private final List<DeviceId> devices;

    public SearchDevicesAdapter(Consumer<DeviceId> listenerAddDevice) {
        this.listenerAddDevice = listenerAddDevice;
        this.devices = new ArrayList<>();
    }

    @SuppressLint("NotifyDataSetChanged")
    public void setDevices(Collection<DeviceId> devices) {
        this.devices.clear();
        if (devices != null && devices.size() > 0){
            this.devices.addAll(devices);
        }

        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public SearchDevicesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.view_add_device_item, parent, false);
        return new SearchDevicesViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SearchDevicesViewHolder holder, int position) {
        DeviceId deviceId = devices.get(position);

        if (deviceId.getDeviceType() == DeviceType.SHIMANO_SHIFTING) {
            holder.getImageViewIcon().setImageResource(R.drawable.ic_di2);
            String deviceLabel = holder.getTextViewName().getContext().getString(R.string.text_param_di2_name, deviceId.getName());
            holder.getTextViewName().setText(deviceLabel);
            holder.getButtonAdd().setVisibility(View.VISIBLE);
        } else {
            holder.getImageViewIcon().setImageResource(R.drawable.ic_memory);
            String deviceLabel = holder.getTextViewName().getContext().getString(R.string.text_param_sensor_name, deviceId.getName());
            holder.getTextViewName().setText(deviceLabel);
            holder.getButtonAdd().setVisibility(View.INVISIBLE);
        }

        holder.getButtonAdd().setOnClickListener(e -> listenerAddDevice.accept(deviceId));
    }

    @Override
    public int getItemCount() {
        return devices.size();
    }

}
