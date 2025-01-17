package com.valterc.ki2.karoo.datatypes

import com.valterc.ki2.karoo.Ki2ExtensionContext
import com.valterc.ki2.karoo.views.GearsExtensionView

@Suppress("unused")
class GearsSizeVisualDataType(
    extensionContext: Ki2ExtensionContext
) : RenderedVisualDataType("DATATYPE_VISUAL_GEARS_SIZE", extensionContext,
    { GearsExtensionView(extensionContext, showGearSize = true) })