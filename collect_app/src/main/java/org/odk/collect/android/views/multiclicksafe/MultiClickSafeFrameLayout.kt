package org.odk.collect.android.views.multiclicksafe

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import org.odk.collect.android.utilities.MultiClickGuard

class MultiClickSafeFrameLayout : FrameLayout {
    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)

    override fun performClick(): Boolean {
        return MultiClickGuard.allowClick() && super.performClick()
    }
}
