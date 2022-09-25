package com.valterc.ki2.karoo.formatters;

import androidx.annotation.NonNull;

import java.text.DecimalFormat;

import io.hammerhead.sdk.v0.datatype.formatter.SdkFormatter;

public class BatteryTextFormatter extends SdkFormatter {

    private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("###.#");

    @NonNull
    @Override
    public String formatValue(double value) {
        return DECIMAL_FORMAT.format(value);
    }

}
