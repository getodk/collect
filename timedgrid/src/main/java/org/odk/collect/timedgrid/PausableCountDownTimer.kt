package org.odk.collect.timedgrid

import android.os.CountDownTimer

class PausableCountDownTimer {
    private var millisRemaining: Long = 0
    private lateinit var onTick: (millisUntilFinished: Long) -> Unit
    private lateinit var onFinish: () -> Unit

    private var timer: CountDownTimer? = null
    private var isPaused: Boolean = true

    fun setUpListeners(onTick: (millisUntilFinished: Long) -> Unit, onFinish: () -> Unit) {
        this.onTick = onTick
        this.onFinish = onFinish
    }

    fun setUpDuration(millisRemaining: Long) {
        this.millisRemaining = millisRemaining
    }

    /**
     * Starts or resumes the countdown.
     * @return This PausableCountDownTimer.
     */
    @Synchronized
    fun start(): PausableCountDownTimer {
        if (isPaused) {
            isPaused = false
            timer = object : CountDownTimer(millisRemaining, 1000) {
                override fun onTick(millisUntilFinished: Long) {
                    millisRemaining = millisUntilFinished
                    this@PausableCountDownTimer.onTick(millisRemaining)
                }

                override fun onFinish() {
                    millisRemaining = 0
                    this@PausableCountDownTimer.onFinish()
                }
            }.start()
        }
        return this
    }

    /**
     * Pauses the countdown.
     */
    @Synchronized
    fun pause() {
        if (!isPaused) {
            timer?.cancel()
            timer = null
        }
        isPaused = true
    }

    /**
     * Cancels the countdown and resets the timer.
     */
    @Synchronized
    fun cancel() {
        timer?.cancel()
        timer = null
        isPaused = true
    }

    fun getMillisRemaining(): Long {
        return millisRemaining
    }
}
