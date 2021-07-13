package org.odk.collect.android.preferences

import androidx.lifecycle.ViewModel

class ProjectPreferencesViewModel : ViewModel() {
    enum class State {
        LOCKED, // Admin password is set but not entered
        UNLOCKED, // Admin password is set and entered
        NOT_PROTECTED // Admin password is not set
    }

    private var state: State? = null

    fun initState(isPasswordSet: Boolean) {
        if (state == null) {
            state = if (isPasswordSet) {
                State.LOCKED
            } else {
                State.NOT_PROTECTED
            }
        }
    }

    fun isStateLocked() = state == State.LOCKED

    fun isStateUnlocked() = state == State.UNLOCKED

    fun setStateUnlocked() {
        state = State.UNLOCKED
    }

    fun setStateNotProtected() {
        state = State.NOT_PROTECTED
    }
}
