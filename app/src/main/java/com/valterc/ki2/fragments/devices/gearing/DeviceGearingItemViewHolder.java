package com.valterc.ki2.fragments.devices.gearing;

import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.valterc.ki2.R;
import com.valterc.ki2.utils.function.ThrowingBiConsumer;

public class DeviceGearingItemViewHolder extends RecyclerView.ViewHolder {

    private final EditText editTextGear;
    private final TextView textViewIndex;
    private final DeviceGearingItemTextWatcher textWatcher;

    public DeviceGearingItemViewHolder(@NonNull View itemView, @NonNull ThrowingBiConsumer<Integer, String> textChangedListener) {
        super(itemView);

        editTextGear = itemView.findViewById(R.id.edittext_gearing_gear);
        textViewIndex = itemView.findViewById(R.id.textview_gearing_index);

        textWatcher = new DeviceGearingItemTextWatcher(editTextGear, textChangedListener);
        editTextGear.addTextChangedListener(textWatcher);
    }

    public EditText getEditTextGear() {
        return editTextGear;
    }

    public TextView getTextViewIndex() {
        return textViewIndex;
    }

    public DeviceGearingItemTextWatcher getTextWatcher() {
        return textWatcher;
    }
}