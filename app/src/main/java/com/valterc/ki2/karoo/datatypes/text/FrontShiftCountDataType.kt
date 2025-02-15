package com.valterc.ki2.karoo.datatypes.text

import com.valterc.ki2.karoo.Ki2ExtensionContext
import com.valterc.ki2.karoo.shifting.ShiftCountHandler

class FrontShiftCountDataType(extensionContext: Ki2ExtensionContext) :
    BaseShiftCountDataType(extensionContext, "DATATYPE_FRONT_SHIFT_COUNT") {

    override fun getShiftCountValue(shiftCountHandler: ShiftCountHandler): Int {
        return shiftCountHandler.frontShiftCount
    }

}