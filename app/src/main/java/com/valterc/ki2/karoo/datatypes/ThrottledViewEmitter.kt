package com.valterc.ki2.karoo.datatypes

import android.os.DeadObjectException
import android.widget.RemoteViews
import io.hammerhead.karooext.internal.Emitter
import io.hammerhead.karooext.internal.ViewEmitter
import io.hammerhead.karooext.models.ViewEvent
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber
import kotlin.time.Duration.Companion.milliseconds

/**
 * Drop-in replacement for [ViewEmitter] that prevents view updates from being lost.
 *
 * karoo-ext silently discards any view emitted less than 900ms after the previous one. When the
 * discarded update happens to be the last one of a burst, the UI is left showing stale data until
 * something else triggers an update. This wrapper conflates instead of dropping: only the most
 * recent view is retained, and it is always emitted once the rate limit window has elapsed.
 *
 * All other [Emitter] operations are delegated to the wrapped emitter unchanged.
 *
 * karoo-ext never detects death of the Karoo System process, so a crash there leaves this emitter
 * running against a dead binder. Updates that fail with [DeadObjectException] therefore cancel the
 * emitter, which releases the resources held by the data type. The Karoo System calls `startView`
 * again once it restarts, creating a new emitter.
 */
class ThrottledViewEmitter(private val emitter: ViewEmitter) : Emitter<ViewEvent> by emitter {

    private val scope = CoroutineScope(Dispatchers.IO)
    private val views = Channel<RemoteViews>(Channel.CONFLATED)

    init {
        scope.launch {
            for (view in views) {
                try {
                    emitter.updateView(view)
                } catch (e: DeadObjectException) {
                    Timber.w(e, "Unable to update view, Karoo System is no longer available, stopping view updates")
                    emitter.cancel()
                    return@launch
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    Timber.w(e, "Unable to update view")
                }

                delay(UPDATE_INTERVAL_MS.milliseconds)
            }
        }
    }

    fun updateView(view: RemoteViews) {
        views.trySend(view)
    }

    override fun setCancellable(cancellable: () -> Unit) {
        emitter.setCancellable {
            stop()
            cancellable()
        }
    }

    override fun onComplete() {
        stop()
        emitter.onComplete()
    }

    private fun stop() {
        views.close()
        scope.cancel()
    }

    private companion object {
        /**
         * karoo-ext limits view updates to ~1Hz (900ms), emit slightly slower to stay above it.
         */
        const val UPDATE_INTERVAL_MS = 1_000L
    }
}
