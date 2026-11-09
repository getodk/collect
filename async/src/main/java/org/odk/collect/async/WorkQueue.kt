package org.odk.collect.async

import kotlinx.coroutines.sync.Mutex

class WorkQueue<T>(private val onClear: (List<T>) -> Unit = {}) {

    private var itemList = mutableListOf<T>()
    private var lock = Mutex()

    fun consume(consumer: (List<T>) -> Unit) {
        lock.tryAcquire { acquired ->
            if (acquired) {
                val consumed = itemList.toList()
                itemList.clear()

                consumer(consumed)
                onClear(consumed)
            }
        }
    }

    fun <U> set(block: WorkQueueScope<T>.() -> U): U {
        return lock.tryAcquire { acquired ->
            if (acquired) {
                val scope = WorkQueueScopeImpl(itemList, onClear)
                block(scope)
            } else {
                block(WorkQueueScopeLocked())
            }
        }
    }

    private fun <U> Mutex.tryAcquire(work: (Boolean) -> U): U {
        val acquired = this.tryLock()
        try {
            return work(acquired)
        } finally {
            if (acquired) {
                this.unlock()
            }
        }
    }
}

interface WorkQueueScope<T> {
    fun replace(items: List<T>)
}

private class WorkQueueScopeImpl<T>(
    private val itemList: MutableList<T>,
    private val onClear: (List<T>) -> Unit
) : WorkQueueScope<T> {

    override fun replace(items: List<T>) {
        onClear(itemList.toList())
        itemList.clear()
        itemList.addAll(items)
    }
}

private class WorkQueueScopeLocked<T> : WorkQueueScope<T> {
    override fun replace(items: List<T>) {
    }
}

fun <T> WorkQueue<T>.clearAndBlock(work: () -> Unit) {
    this.consume {
        work()
    }
}
