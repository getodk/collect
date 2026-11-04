package org.odk.collect.async

import kotlinx.coroutines.sync.Mutex

/**
 * @param afterConsume run after the queue is consumed with the values consume. Allows any
 * resources releated to the items to be cleaned up.
 */
class WorkQueue<T>(private val afterConsume: (List<T>) -> Unit = {}) {

    private var itemList = mutableListOf<T>()
    private var lock = Mutex()

    fun consume(consumer: (List<T>) -> Unit): Boolean {
        lock.tryAcquire {
            val consumed = itemList.toList()
            itemList.clear()

            consumer(consumed)
            afterConsume(consumed)
        }

        return false
    }

    fun add(producer: () -> List<T>): Boolean {
        return lock.tryAcquire {
            itemList.addAll(producer())
        }
    }

    private fun <U> Mutex.tryAcquire(work: () -> U): Boolean {
        val acquired = this.tryLock()
        try {
            if (acquired) {
                work()
                return true
            } else {
                return false
            }
        } finally {
            if (acquired) {
                this.unlock()
            }
        }
    }
}

fun <T> WorkQueue<T>.clearAndBlock(work: () -> Unit) {
    this.consume { work() }
}
