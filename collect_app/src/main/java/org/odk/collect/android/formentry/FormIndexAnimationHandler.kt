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
        if (lastIndex == null) {
            listener.onScreenRefresh(true)
        } else {
            if (index > lastIndex) {
                listener.onScreenChange(Direction.FORWARDS)
            } else if (index < lastIndex) {
                listener.onScreenChange(Direction.BACKWARDS)
            } else {
                listener.onScreenRefresh(false)
            }
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
