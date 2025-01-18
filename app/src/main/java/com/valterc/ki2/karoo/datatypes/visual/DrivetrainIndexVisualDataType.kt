package com.valterc.ki2.karoo.datatypes.visual

import com.valterc.ki2.karoo.Ki2ExtensionContext
import com.valterc.ki2.karoo.datatypes.RenderedVisualDataType
import com.valterc.ki2.karoo.views.DrivetrainExtensionView

@Suppress("unused")
class DrivetrainIndexVisualDataType(
    extensionContext: Ki2ExtensionContext
) : RenderedVisualDataType("DATATYPE_VISUAL_DRIVETRAIN_INDEX", extensionContext,
    { DrivetrainExtensionView(extensionContext) })