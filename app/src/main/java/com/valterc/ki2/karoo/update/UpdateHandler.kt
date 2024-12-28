package com.valterc.ki2.karoo.update

import com.valterc.ki2.R
import com.valterc.ki2.data.message.UpdateAvailableMessage
import com.valterc.ki2.data.update.UpdateStateStore
import com.valterc.ki2.karoo.Ki2ExtensionContext
import com.valterc.ki2.karoo.RideHandler
import io.hammerhead.karooext.models.SystemNotification
import timber.log.Timber
import java.util.function.Consumer

class UpdateHandler(extensionContext: Ki2ExtensionContext) : RideHandler(extensionContext) {

    private val onUpdateAvailableMessage =
        Consumer { updateAvailableMessage: UpdateAvailableMessage ->
            this.onUpdateAvailableMessage(
                updateAvailableMessage
            )
        }

    init {
        extensionContext.serviceClient.customMessageClient
            .registerUpdateAvailableWeakListener(onUpdateAvailableMessage)

        val ongoingUpdateStateInfo = UpdateStateStore.getAndClearOngoingUpdateState(extensionContext.context)
        if (ongoingUpdateStateInfo != null) {
            Timber.i("Update complete")
            extensionContext.karooSystem.dispatch(SystemNotification(
                "ki2-update-complete",
                message = extensionContext.context.getString(R.string.text_update_complete_restart),
                header = extensionContext.context.getString(R.string.text_ki2_updated),
                action = extensionContext.context.getString(R.string.app_name),
            ))
        }
    }

    private fun onUpdateAvailableMessage(updateAvailableMessage: UpdateAvailableMessage) {
        Timber.d("Update available, showing notification")
        extensionContext.karooSystem.dispatch(SystemNotification(
            "ki2-update",
            message = extensionContext.context.getString(R.string.text_param_update_available_version, updateAvailableMessage.releaseInfo.name),
            header = extensionContext.context.getString(R.string.text_update_ki2),
            action = extensionContext.context.getString(R.string.text_update),
            actionIntent = "com.valterc.ki2.action.UPDATE"
            ))
    }
}
