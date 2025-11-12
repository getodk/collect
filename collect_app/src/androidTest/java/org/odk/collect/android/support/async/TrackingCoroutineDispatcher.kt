package org.odk.collect.android.support.async

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Runnable
import org.odk.collect.android.support.async.AsyncWorkTracker
import kotlin.coroutines.CoroutineContext

class TrackingCoroutineDispatcher(private val dispatcher: CoroutineDispatcher) : CoroutineDispatcher() {
    override fun dispatch(
        context: CoroutineContext,
        block: Runnable
    ) {
        AsyncWorkTracker.startWork()
        dispatcher.dispatch(context) {
            try {
                block.run()
            } finally {
                AsyncWorkTracker.finishWork()
            }
        }
    }
}
