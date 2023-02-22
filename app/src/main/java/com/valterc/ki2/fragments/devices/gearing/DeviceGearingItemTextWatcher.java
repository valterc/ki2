package com.valterc.ki2.fragments.devices.gearing;

import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;

import com.valterc.ki2.utils.function.ThrowingBiConsumer;

public class DeviceGearingItemTextWatcher implements TextWatcher {

    private final EditText editText;
    private final ThrowingBiConsumer<Integer, String> textChangedListener;

    private int position;

    public DeviceGearingItemTextWatcher(EditText editText, ThrowingBiConsumer<Integer, String> textChangedListener) {
        this.editText = editText;
        this.textChangedListener = textChangedListener;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    @Override
    public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {
    }

    @Override
    public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
        try {
            textChangedListener.accept(position, charSequence.toString());
            editText.setError(null);
        } catch (Exception e) {
            editText.setError(e.getMessage());
        }
    }

    @Override
    public void afterTextChanged(Editable editable) {
    }

}
