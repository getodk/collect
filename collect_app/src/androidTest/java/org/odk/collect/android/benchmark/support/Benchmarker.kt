package org.odk.collect.android.benchmark.support

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.lessThan
import org.odk.collect.android.support.pages.Page
import org.odk.collect.shared.TimeInMs

class Benchmarker {
    private val stopwatch = Stopwatch()
    private val targets = mutableMapOf<String, Long>()

    fun <T> benchmark(name: String, target: Long, action: () -> T): T {
        targets[name] = target
        return stopwatch.time(name) {
            action()
        }
    }

    fun assertResults() {
        printResults()

        targets.entries.forEach {
            val time = stopwatch.getTime(it.key)
            assertThat("\"${it.key}\" took ${time}s!", time, lessThan(it.value))
        }
    }

    private fun printResults() {
        println("Benchmark results:")
        targets.keys.forEach {
            println("$it: ${stopwatch.getTime(it)}s")
        }
    }
}

fun <T : Page<T>, Y : Page<Y>> Y.benchmark(
    name: String,
    target: Long,
    benchmarker: Benchmarker,
    action: (Y) -> T
): T {
    return benchmarker.benchmark(name, target) {
        action(this)
    }
}

private class Stopwatch {

    private val times = mutableMapOf<String, Long>()

    fun <T> time(name: String, action: () -> T): T {
        val startTime = System.currentTimeMillis()
        val result = action()
        val endTime = System.currentTimeMillis()

        times[name] = (endTime - startTime) / TimeInMs.ONE_SECOND
        return result
    }

    fun getTime(name: String): Long {
        return times[name]!!
    }
}
