package org.odk.collect.android.views

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import org.odk.collect.android.R
import org.odk.collect.androidshared.ui.Animations.createAlphaAnimation

class TransparentProgressScreen(context: Context, attrs: AttributeSet?) :
    FrameLayout(context, attrs) {

    init {
        inflate(context, R.layout.transparent_progress_screen, this)
    }

    var visible: Boolean = false
        set(value) {
            if (value) {
                show()
            } else {
                hide()
            }

            field = value
        }

    fun show() {
        alpha = 0f
        visibility = VISIBLE
        createAlphaAnimation(0f, 1f, 60).start()
    }

    fun hide() {
        visibility = GONE
    }
}
