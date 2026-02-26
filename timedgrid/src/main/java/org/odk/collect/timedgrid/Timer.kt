package org.odk.collect.timedgrid

interface Timer {
    fun setUpListeners(
        onTick: (millisUntilFinished: Long) -> Unit,
        onFinish: () -> Unit
    )

    fun setUpDuration(millisRemaining: Long)

    fun start(): Timer

    fun pause()

    fun cancel()

    fun getMillisRemaining(): Long
}

object TimerProvider {
    var factory: () -> Timer = { PausableCountDownTimer() }

    fun get(): Timer = factory()
}
