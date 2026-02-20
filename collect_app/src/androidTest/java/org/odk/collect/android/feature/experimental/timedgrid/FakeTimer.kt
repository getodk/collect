package org.odk.collect.android.feature.experimental.timedgrid

import android.os.Handler
import android.os.Looper
import org.odk.collect.experimental.timedgrid.Timer

class FakeTimer : Timer {
    private val handler = Handler(Looper.getMainLooper())
    private var millisRemaining: Long = 0
    private var isPaused = false

    private lateinit var onTick: (millisUntilFinished: Long) -> Unit
    private lateinit var onFinish: () -> Unit

    override fun setUpListeners(onTick: (millisUntilFinished: Long) -> Unit, onFinish: () -> Unit) {
        this.onTick = onTick
        this.onFinish = onFinish
    }

    override fun setUpDuration(millisRemaining: Long) {
        this.millisRemaining = millisRemaining
    }

    override fun start(): Timer {
        if (!isPaused) {
            isPaused = false
            return FakeTimer().also {
                handler.post {
                    onTick(millisRemaining)
                }
            }
        }
        return this
    }

    override fun pause() {
        isPaused = true
    }

    override fun cancel() {
        isPaused = false
    }

    override fun getMillisRemaining(): Long {
        return millisRemaining
    }

    fun wait(seconds: Int) {
        if (isPaused) return

        repeat(seconds) {
            millisRemaining -= 1000
            if (millisRemaining <= 0) {
                millisRemaining = 0
                handler.post { onFinish() }
                return
            } else {
                handler.post { onTick(millisRemaining) }
            }
        }
    }
}
