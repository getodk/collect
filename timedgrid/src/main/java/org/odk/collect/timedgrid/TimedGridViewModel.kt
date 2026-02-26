package org.odk.collect.timedgrid

import androidx.lifecycle.ViewModel
import org.javarosa.core.model.FormIndex

class TimedGridViewModel : ViewModel() {

    data class TimedGridTimerState(
        val millisRemaining: Long,
        val state: TimedGridState,
        val toggledAnswers: Set<String> = emptySet(),
        val lastAttempted: String? = null
    )

    private val timedGridStates = mutableMapOf<FormIndex, TimedGridTimerState>()

    fun saveTimedGridState(index: FormIndex, state: TimedGridTimerState) {
        timedGridStates[index] = state
    }

    fun getTimedGridState(index: FormIndex): TimedGridTimerState? {
        return timedGridStates[index]
    }

    override fun onCleared() {
        super.onCleared()
        timedGridStates.clear()
    }
}
