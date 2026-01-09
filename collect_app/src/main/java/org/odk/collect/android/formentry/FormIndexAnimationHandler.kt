package org.odk.collect.android.formentry

import org.javarosa.core.model.FormIndex

/**
 * Responsible for determining how a new "screen" in a form should be animated to based on
 * the [FormIndex].
 */
class FormIndexAnimationHandler(private val listener: Listener) {

    enum class Direction {
        FORWARDS, BACKWARDS
    }

    private var lastIndex: FormIndex? = null

    fun handle(index: FormIndex) {
        lastIndex?.let {
            if (index.toString().startsWith(it.toString())) {
                listener.onScreenRefresh(false)
            } else if (index > it) {
                listener.onScreenChange(Direction.FORWARDS)
            } else if (index < it) {
                listener.onScreenChange(Direction.BACKWARDS)
            } else {
                listener.onScreenRefresh(false)
            }
        } ?: run {
            listener.onScreenRefresh(true)
        }

        lastIndex = index
    }

    /**
     * Can be used to update the handler on the starting index in situations
     * where [.handle] isn't be called.
     */
    fun setLastIndex(lastIndex: FormIndex?) {
        this.lastIndex = lastIndex
    }

    interface Listener {
        fun onScreenChange(direction: Direction?)
        fun onScreenRefresh(isFormStart: Boolean)
    }
}
