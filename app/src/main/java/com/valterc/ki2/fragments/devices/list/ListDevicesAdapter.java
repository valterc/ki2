package com.valterc.ki2.fragments.devices.list;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.MotionEvent;
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
    private final Consumer<RecyclerView.ViewHolder> listenerDragStart;
    private final List<DeviceId> devices;
    private final Map<DeviceId, DevicePreferences> devicePreferencesMap;
    private final Map<DeviceId, ConnectionDataInfo> connectionDataInfoMap;

    public ListDevicesAdapter(Context context, Consumer<DeviceId> listenerConfigureDevice, Consumer<DeviceId> listenerReconnectDevice, Consumer<RecyclerView.ViewHolder> listenerDragStart) {
        this.context = context;
        this.listenerConfigureDevice = listenerConfigureDevice;
        this.listenerReconnectDevice = listenerReconnectDevice;
        this.listenerDragStart = listenerDragStart;
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

        for (DeviceId device : this.devices) {
            devicePreferencesMap.put(device, new DevicePreferences(context, device));
        }

        notifyDataSetChanged();
    }

    public List<DeviceId> getDevices() {
        return new ArrayList<>(devices);
    }

    public void setConnectionDataInfo(Map<DeviceId, ConnectionDataInfo> connectionDataInfoMap) {
        for (int i = 0; i < devices.size(); i++) {
            DeviceId deviceId = devices.get(i);
            ConnectionDataInfo existingConnectionDataInfo = this.connectionDataInfoMap.get(deviceId);
            ConnectionDataInfo newConnectionDataInfo = connectionDataInfoMap.get(deviceId);

            if (newConnectionDataInfo != null &&
                    (existingConnectionDataInfo == null || existingConnectionDataInfo.getConnectionStatus() != newConnectionDataInfo.getConnectionStatus())) {
                this.connectionDataInfoMap.put(deviceId, newConnectionDataInfo);
                notifyItemChanged(i);
            }
        }
    }

    @NonNull
    @Override
    @SuppressLint("ClickableViewAccessibility")
    public ListDevicesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.view_list_devices_item, parent, false);
        ListDevicesViewHolder viewHolder = new ListDevicesViewHolder(view);

        viewHolder.getImageViewDrag().setOnTouchListener((v, event) -> {
            if (event.getActionMasked() == MotionEvent.ACTION_DOWN) {
                listenerDragStart.accept(viewHolder);
            }

            return true;
        });

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ListDevicesViewHolder holder, int position) {
        DeviceId deviceId = devices.get(position);
        DevicePreferences devicePreferences = Objects.requireNonNull(devicePreferencesMap.get(deviceId));
        holder.getTextViewName().setText(devicePreferences.getName());

        if (deviceId.getDeviceType() == DeviceType.SHIMANO_SHIFTING) {
            holder.getImageViewIcon().setImageResource(R.drawable.ic_di2);
        } else if (deviceId.getDeviceType() == DeviceType.MOCK_SHIFTING) {
            holder.getImageViewIcon().setImageResource(R.drawable.ic_mock);
        } else {
            holder.getImageViewIcon().setImageResource(R.drawable.ic_memory);
        }

        setConnectionStatusIndicator(holder, connectionDataInfoMap.get(deviceId), devicePreferences.isEnabled());
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

    private void setConnectionStatusIndicator(ListDevicesViewHolder holder, ConnectionDataInfo connectionDataInfo, boolean enabled) {
        Context context = holder.getTextViewConnectionStatus().getContext();

        if (!enabled) {
            holder.getTextViewConnectionStatus().setText(R.string.text_disabled);
            holder.getTextViewConnectionStatus().setTextColor(context.getColor(R.color.hh_faded_gray));
            holder.getTextViewName().setTextColor(context.getColor(R.color.hh_dark_grey));
            holder.getButtonReconnect().setVisibility(View.INVISIBLE);
            return;
        }

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

    public void moveDevicePosition(int fromPosition, int toPosition) {
        devices.add(toPosition, devices.remove(fromPosition));
        notifyItemMoved(fromPosition, toPosition);
    }
}
