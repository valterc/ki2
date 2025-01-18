package com.valterc.ki2.karoo.datatypes.visual

import com.valterc.ki2.karoo.Ki2ExtensionContext
import com.valterc.ki2.karoo.datatypes.RenderedVisualDataType
import com.valterc.ki2.karoo.views.BikeBatteryExtensionView

@Suppress("unused")
class BikeBatteryVisualDataType(
    extensionContext: Ki2ExtensionContext
) : RenderedVisualDataType("DATATYPE_VISUAL_BIKE_BATTERY", extensionContext,
    { BikeBatteryExtensionView(extensionContext) })