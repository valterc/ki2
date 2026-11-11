package com.valterc.ki2.karoo.datatypes.visual

import com.valterc.ki2.karoo.Ki2ExtensionContext
import com.valterc.ki2.karoo.datatypes.RenderedVisualDataType
import com.valterc.ki2.karoo.views.SwitchBatteriesExtensionView

class SwitchBatteriesVisualDataType(extensionContext: Ki2ExtensionContext) :
    RenderedVisualDataType("DATATYPE_VISUAL_SWITCH_BATTERIES", extensionContext,
        { SwitchBatteriesExtensionView(extensionContext) })
