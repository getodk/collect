package org.odk.collect.android.preferences

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import org.odk.collect.android.utilities.AdminPasswordProvider
import org.odk.collect.androidshared.data.Consumable

class ProjectPreferencesViewModel(adminPasswordProvider: AdminPasswordProvider) : ViewModel() {
    enum class State {
        LOCKED, // Admin password is set but not entered
        UNLOCKED, // Admin password is set and entered
        NOT_PROTECTED // Admin password is not set
    }

    private var _state = MutableLiveData<Consumable<State>>()
    var state: LiveData<Consumable<State>> = _state

    init {
        _state.value = if (adminPasswordProvider.isAdminPasswordSet) {
            Consumable(State.LOCKED)
        } else {
            Consumable(State.NOT_PROTECTED)
        }
    }

    fun isStateLocked() = state.value == Consumable(State.LOCKED)

    fun isStateUnlocked() = state.value == Consumable(State.UNLOCKED)

    fun isStateNotProtected() = state.value == Consumable(State.NOT_PROTECTED)

    fun setStateLocked() {
        _state.value = Consumable(State.LOCKED)
    }

    fun setStateUnlocked() {
        _state.value = Consumable(State.UNLOCKED)
    }

    fun setStateNotProtected() {
        _state.value = Consumable(State.NOT_PROTECTED)
    }

    open class Factory(private val adminPasswordProvider: AdminPasswordProvider) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ProjectPreferencesViewModel(adminPasswordProvider) as T
        }
    }
}
