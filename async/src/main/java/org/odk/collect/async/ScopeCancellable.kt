package org.odk.collect.async

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel

internal class ScopeCancellable(private val scope: CoroutineScope) : Cancellable {

    override fun cancel(): Boolean {
        scope.cancel()
        return true
    }
}
