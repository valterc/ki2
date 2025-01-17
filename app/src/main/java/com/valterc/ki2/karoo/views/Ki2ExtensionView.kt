package com.valterc.ki2.karoo.views

import android.content.Context
import android.content.res.Configuration
import android.view.LayoutInflater
import android.view.View
import com.valterc.ki2.karoo.Ki2ExtensionContext
import com.valterc.ki2.karoo.service.ServiceClient
import io.hammerhead.karooext.models.ViewConfig

abstract class Ki2ExtensionView(protected val extensionContext: Ki2ExtensionContext) {
    private var viewUpdateListener: Runnable? = null

    protected val context: Context
        get() = extensionContext.context

    protected val serviceClient: ServiceClient
        get() = extensionContext.serviceClient

    protected val karooTheme: KarooTheme
        get() {
            val nightModeFlags = context.resources
                .configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
            return if (nightModeFlags == Configuration.UI_MODE_NIGHT_YES) KarooTheme.DARK else KarooTheme.WHITE
        }

    protected fun viewUpdated() {
        viewUpdateListener?.run()
    }

    protected abstract fun createView(layoutInflater: LayoutInflater, viewConfig: ViewConfig): View

    open fun dispose() {
        setViewUpdateListener(null)
    }

    fun setViewUpdateListener(listener: Runnable?) {
        viewUpdateListener = listener
    }

    fun createView(viewConfig: ViewConfig): View {
        return createView(LayoutInflater.from(context), viewConfig)
    }
}
