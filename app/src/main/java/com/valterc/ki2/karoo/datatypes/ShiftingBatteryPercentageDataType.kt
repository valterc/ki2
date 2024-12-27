package com.valterc.ki2.karoo.datatypes

import io.hammerhead.karooext.extension.DataTypeImpl

class ShiftingBatteryPercentageDataType(extension: String) : DataTypeImpl(extension, TYPE_ID) {

    companion object {
        const val TYPE_ID = "TYPE_SHIFTING_BATTERY_PERCENTAGE"
    }

}