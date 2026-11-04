package org.odk.collect.async

import kotlinx.coroutines.sync.Mutex

class WorkQueue<T> {

    private var itemList = mutableListOf<T>()
    private var lock = Mutex()

    fun consume(consumer: (List<T>) -> Unit): Boolean {
        lock.tryAcquire {
            val consumed = itemList.toList()
            itemList.clear()
            consumer(consumed)
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
