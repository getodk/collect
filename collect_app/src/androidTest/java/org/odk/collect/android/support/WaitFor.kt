package org.odk.collect.android.support

import junit.framework.AssertionFailedError
import java.lang.AssertionError
import java.lang.Exception
import java.lang.RuntimeException
import java.util.concurrent.Callable

object WaitFor {

    @JvmStatic
    fun <T> waitFor(callable: Callable<T>): T {
        var counter = 0
        var failure: Throwable? = null

        // Try 20 times/for 5 seconds
        while (counter < 20) {
            failure = try {
                return callable.call()
            } catch (throwable: Exception) {
                throwable
            } catch (throwable: AssertionFailedError) {
                throwable
            } catch (throwable: AssertionError) {
                throwable
            }

            wait250ms()
            counter++
        }

        throw RuntimeException("waitFor failed", failure)
    }

    @JvmStatic
    fun wait250ms() {
        try {
            Thread.sleep(250)
        } catch (ignored: InterruptedException) {
            // ignored
        }
    }
}
