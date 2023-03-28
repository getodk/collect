package org.odk.collect.android.mainmenu

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import org.odk.collect.android.R
import org.odk.collect.androidshared.ui.multiclicksafe.MultiClickGuard

class StartNewFormButton(context: Context, attrs: AttributeSet?) : FrameLayout(context, attrs) {

    constructor(context: Context) : this(context, null)

    init {
        inflate(context, R.layout.start_new_from_button, this)
    }

    override fun performClick(): Boolean {
        return MultiClickGuard.allowClick() && super.performClick()
    }
}
