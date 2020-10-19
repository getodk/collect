package org.odk.collect.async

interface Cancellable {
    fun cancel(): Boolean
}
