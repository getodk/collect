package org.odk.collect.android.utilities

import android.os.Handler
import kotlinx.coroutines.*
import org.odk.collect.utilities.Cancellable
import org.odk.collect.utilities.Scheduler
import java.lang.Runnable
import java.util.function.Consumer
import java.util.function.Supplier

class HandlerAndCoroutineScheduler : Scheduler {

    private val uiScope = CoroutineScope(Dispatchers.Main)

    override fun <T> scheduleInBackground(task: Supplier<T>, callback: Consumer<T>) {
        uiScope.launch {
            callback.accept(withContext(Dispatchers.IO) { task.get() })
        }
    }

    override fun schedule(task: Runnable, period: Long): Cancellable {
        val handler = Handler()
        handler.post(object : Runnable {
            override fun run() {
                task.run()
                handler.postDelayed(this, period)
            }
        })
        return Cancellable {
            handler.removeCallbacksAndMessages(null)
            true
        }
    }
}