package org.odk.collect.async

import kotlinx.coroutines.Dispatchers

class DefaultDispatcherProvider : DispatcherProvider {
    override val foreground = Dispatchers.Main
    override val background = Dispatchers.IO
}
