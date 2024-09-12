package org.odk.collect.androidshared.ui

import android.content.Context
import android.os.Handler
import android.util.AttributeSet
import com.google.android.material.progressindicator.LinearProgressIndicator

/**
 * A progress bar that shows for a minimum amount fo time so it's obvious to the user that
 * something has happened.
 */
class ObviousProgressBar(
    context: Context,
    attrs: AttributeSet?
) : LinearProgressIndicator(context, attrs) {
    private val handler = Handler()
    private var shownAt: Long? = null

    init {
        super.setVisibility(GONE)
        super.setIndeterminate(true)
    }

    override fun show() {
        handler.removeCallbacksAndMessages(null)
        shownAt = System.currentTimeMillis()
        super.setVisibility(VISIBLE)
    }

    override fun hide() {
        if (shownAt != null) {
            val timeShown = System.currentTimeMillis() - shownAt!!

            if (timeShown < MINIMUM_SHOW_TIME) {
                val delay = MINIMUM_SHOW_TIME - timeShown

                handler.removeCallbacksAndMessages(null)
                handler.postDelayed({ this.makeGone() }, delay)
            } else {
                makeGone()
            }
        } else {
            makeGone()
        }
    }

    private fun makeGone() {
        super.setVisibility(GONE)
        shownAt = null
    }

    companion object {
        private const val MINIMUM_SHOW_TIME = 750
    }
}
