package org.odk.collect.experimental.timedgrid

import android.os.CountDownTimer

class PausableCountDownTimer : Timer {
    private var millisRemaining: Long = 0
    private lateinit var onTick: (millisUntilFinished: Long) -> Unit
    private lateinit var onFinish: () -> Unit

    private var timer: CountDownTimer? = null
    private var isPaused: Boolean = true

    override fun setUpListeners(onTick: (millisUntilFinished: Long) -> Unit, onFinish: () -> Unit) {
        this.onTick = onTick
        this.onFinish = onFinish
    }

    override fun setUpDuration(millisRemaining: Long) {
        this.millisRemaining = millisRemaining
    }

    /**
     * Starts or resumes the countdown.
     * @return This PausableCountDownTimer.
     */
    @Synchronized
    override fun start(): Timer {
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
    override fun pause() {
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
    override fun cancel() {
        timer?.cancel()
        timer = null
        isPaused = true
    }

    override fun getMillisRemaining(): Long {
        return millisRemaining
    }
}
