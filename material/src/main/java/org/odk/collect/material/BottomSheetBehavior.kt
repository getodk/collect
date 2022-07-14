package org.odk.collect.material

import android.view.View
import com.google.android.material.bottomsheet.BottomSheetBehavior.BottomSheetCallback
import com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_HIDDEN

/**
 * Wrapper around [com.google.android.material.bottomsheet.BottomSheetBehavior] that allows animations/dragging to be disabled. This is
 * useful for both UI and Robolectric tests.
 */
class BottomSheetBehavior<V : View> private constructor(private val view: V) {

    private val actual = com.google.android.material.bottomsheet.BottomSheetBehavior.from(view)

    private val callbacks = mutableListOf<BottomSheetCallback>()

    var peekHeight: Int = actual.peekHeight
        set(value) {
            if (DRAGGING_ENABLED) {
                actual.peekHeight = value
            }

            field = value
        }
        get() {
            return if (DRAGGING_ENABLED) {
                actual.peekHeight
            } else {
                field
            }
        }

    var state: Int = actual.state
        set(value) {
            if (DRAGGING_ENABLED) {
                actual.state = value
            } else {
                if (value != STATE_HIDDEN) {
                    view.visibility = View.VISIBLE
                } else {
                    view.visibility = View.GONE
                }

                if (value != field) {
                    callbacks.forEach {
                        it.onStateChanged(view, value)
                    }

                    field = value
                }
            }
        }
        get() {
            return if (DRAGGING_ENABLED) {
                actual.state
            } else {
                field
            }
        }

    fun removeBottomSheetCallback(callback: BottomSheetCallback) {
        callbacks.remove(callback)
        actual.removeBottomSheetCallback(callback)
    }

    fun addBottomSheetCallback(callback: BottomSheetCallback) {
        callbacks.add(callback)
        actual.addBottomSheetCallback(callback)
    }

    companion object {

        var DRAGGING_ENABLED = true

        fun <V : View> from(view: V): BottomSheetBehavior<V> {
            return BottomSheetBehavior(view)
        }
    }
}
