package org.odk.collect.android.views

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import org.odk.collect.android.R

class TransparentProgressScreen(context: Context, attrs: AttributeSet?) :
    FrameLayout(context, attrs) {

    init {
        inflate(context, R.layout.transparent_progress_screen, this)
    }
}
