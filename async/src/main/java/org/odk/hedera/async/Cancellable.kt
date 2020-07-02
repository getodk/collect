package org.odk.hedera.async

interface Cancellable {
    fun cancel(): Boolean
}