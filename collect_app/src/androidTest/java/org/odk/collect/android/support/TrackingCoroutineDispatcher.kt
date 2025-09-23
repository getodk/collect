package org.odk.collect.android.support

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Runnable
import kotlin.coroutines.CoroutineContext

class TrackingCoroutineDispatcher(private val dispatcher: CoroutineDispatcher) : CoroutineDispatcher() {
    override fun dispatch(
        context: CoroutineContext,
        block: Runnable
    ) {
        AsyncWorkTracker.startWork()
        dispatcher.dispatch(context) {
            block.run()
            AsyncWorkTracker.finishWork()
        }
    }
}
