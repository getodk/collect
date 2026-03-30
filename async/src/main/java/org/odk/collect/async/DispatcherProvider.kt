package org.odk.collect.async

import kotlinx.coroutines.CoroutineDispatcher

interface DispatcherProvider {
    val foreground: CoroutineDispatcher
    val background: CoroutineDispatcher
}
