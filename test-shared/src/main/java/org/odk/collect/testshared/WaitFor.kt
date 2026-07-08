package org.odk.collect.testshared

import junit.framework.AssertionFailedError
import org.odk.collect.shared.TimeInMs
import java.util.concurrent.Callable

object WaitFor {

    /**
     * Runs [assertion] repeatedly until no [Exception] is thrown or the [timeout] expires. The
     * [assertion] will always be run at least twice - this accounts for the actual run time
     * taking the full [timeout] period.
     */
    @JvmStatic
    fun <T> waitFor(timeout: Long = 5 * TimeInMs.ONE_SECOND, assertion: Callable<T>): T {
        var failure: Throwable? = null
        val startTime = System.currentTimeMillis()
        var checks = 0

        while ((System.currentTimeMillis() - startTime) < (timeout) || checks < 2) {
            failure = try {
                return assertion.call()
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
