package org.odk.collect.android.support.pages

import org.odk.collect.testshared.WaitFor

class AsyncPage<T : Page<T>>(private val destination: T) : Page<T>() {
    override fun assertOnPage(): T {
        return WaitFor.waitFor {
            destination.assertOnPage()
        }
    }
}
