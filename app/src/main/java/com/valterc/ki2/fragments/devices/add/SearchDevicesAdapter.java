package com.valterc.ki2.fragments.devices.add;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.valterc.ki2.R;
import com.valterc.ki2.data.device.DeviceId;
import com.valterc.ki2.data.device.DeviceName;

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
        if (devices != null && devices.size() > 0) {
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

        switch (deviceId.getDeviceType()) {
            case SHIMANO_SHIFTING:
                holder.getImageViewIcon().setImageResource(R.drawable.ic_di2);
                holder.getButtonAdd().setVisibility(View.VISIBLE);
                break;

            case SHIMANO_EBIKE:
                holder.getImageViewIcon().setImageResource(R.drawable.ic_steps);
                holder.getButtonAdd().setVisibility(View.VISIBLE);
                break;

            case MOCK_SHIFTING:
                holder.getImageViewIcon().setImageResource(R.drawable.ic_mock);
                holder.getButtonAdd().setVisibility(View.VISIBLE);
                break;

            case UNKNOWN:
            default:
                holder.getImageViewIcon().setImageResource(R.drawable.ic_memory);
                holder.getButtonAdd().setVisibility(View.INVISIBLE);
                break;
        }

        holder.getTextViewName().setText(DeviceName.getDefaultName(holder.itemView.getContext(), deviceId));
        holder.getButtonAdd().setOnClickListener(e -> listenerAddDevice.accept(deviceId));
    }

    @Override
    public int getItemCount() {
        return devices.size();
    }

}
