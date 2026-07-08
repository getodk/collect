package org.odk.collect.testshared

import junit.framework.AssertionFailedError
import org.odk.collect.shared.TimeInMs
import java.util.concurrent.Callable

object WaitFor {

    @JvmStatic
    fun <T> waitFor(callable: Callable<T>): T {
        var failure: Throwable? = null
        val startTime = System.currentTimeMillis()
        var checks = 0

        // Try for 5 seconds or at least 2 checks
        while ((System.currentTimeMillis() - startTime) < (5 * TimeInMs.ONE_SECOND) || checks < 2) {
            failure = try {
                return callable.call()
            } catch (throwable: Exception) {
                throwable
            } catch (throwable: AssertionFailedError) {
                throwable
            } catch (throwable: AssertionError) {
                throwable
            }

            Thread.sleep(10)
            checks++
        }

        throw failure!!
    }

    @JvmStatic
    @JvmOverloads
    fun tryAgainOnFail(maxTimes: Int = 2, action: Runnable) {
        var failure: Throwable? = null
        for (i in 0 until maxTimes) {
            try {
                action.run()
                return
            } catch (e: Throwable) {
                failure = e
                Thread.sleep(250)
            }
        }

        throw RuntimeException("tryAgainOnFail failed", failure)
    }
}
