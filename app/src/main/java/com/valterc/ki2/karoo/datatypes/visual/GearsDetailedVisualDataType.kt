package com.valterc.ki2.karoo.datatypes.visual

import com.valterc.ki2.karoo.Ki2ExtensionContext
import com.valterc.ki2.karoo.datatypes.RenderedVisualDataType
import com.valterc.ki2.karoo.views.GearsDetailedExtensionView

@Suppress("unused")
class GearsDetailedVisualDataType(
    extensionContext: Ki2ExtensionContext
) : RenderedVisualDataType("DATATYPE_VISUAL_GEARS_DETAILED", extensionContext,
    { GearsDetailedExtensionView(extensionContext) })