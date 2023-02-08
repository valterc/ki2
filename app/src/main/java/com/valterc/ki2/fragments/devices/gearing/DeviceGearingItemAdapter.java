package com.valterc.ki2.fragments.devices.gearing;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.valterc.ki2.R;

import java.util.Arrays;
import java.util.Objects;
import java.util.function.Consumer;

public class DeviceGearingItemAdapter extends RecyclerView.Adapter<DeviceGearingItemViewHolder> {

    private String[] gears;
    private final Consumer<int[]> onValidatedGears;

    public DeviceGearingItemAdapter(Consumer<int[]> onValidatedGears) {
        this.onValidatedGears = onValidatedGears;
    }

    @SuppressLint("NotifyDataSetChanged")
    public void setGears(int[] gears) {
        this.gears = Arrays.stream(gears).mapToObj(String::valueOf).toArray(String[]::new);
        notifyDataSetChanged();
        if (validateGears()) {
            onValidatedGears.accept(gears);
        }
    }

    @NonNull
    @Override
    public DeviceGearingItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.view_item_device_gearing_gear, parent, false);
        return new DeviceGearingItemViewHolder(view, this::updateGear);
    }

    @Override
    public void onBindViewHolder(@NonNull DeviceGearingItemViewHolder holder, int position) {
        if (gears == null) {
            return;
        }

        holder.getTextViewIndex().setText(String.valueOf(position + 1));
        holder.getTextWatcher().setPosition(position);
        holder.getEditTextGear().setText(gears[position]);
    }

    @Override
    public int getItemCount() {
        return gears == null ? 0 : gears.length;
    }

    private void updateGear(int position, String gear) throws Exception {
        if (isGearInvalid(gear)) {
            gears[position] = gear;
            throw new Exception("Invalid gear");
        }

        if (gears == null) {
            return;
        }

        if (!Objects.equals(gears[position], gear)) {
            gears[position] = gear;
            if (validateGears()) {
                onValidatedGears.accept(Arrays.stream(gears).mapToInt(Integer::parseInt).toArray());
            }
        }
    }

    private boolean isGearInvalid(String gear) {
        int gearInt;
        try {
            gearInt = Integer.parseInt(gear);
        } catch (Exception e) {
            return true;
        }

        return gearInt < 5 || gearInt > 90;
    }

    public boolean validateGears() {
        if (gears == null) {
            return false;
        }

        boolean result = true;
        for (String gear : gears) {
            if (isGearInvalid(gear)) {
                result = false;
            }
        }

        return result;
    }

    public int getFirstInvalidGearIndex() {
        if (gears == null) {
            return -1;
        }

        for (int i = 0; i < gears.length; i++) {
            if (isGearInvalid(gears[i])) {
                return i;
            }
        }

        return -1;
    }

}
