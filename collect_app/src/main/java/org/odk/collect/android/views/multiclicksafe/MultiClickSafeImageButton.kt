package org.odk.collect.android.views.multiclicksafe

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatImageButton
import org.odk.collect.android.utilities.MultiClickGuard.allowClick

class MultiClickSafeImageButton : AppCompatImageButton {
    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet?) : super(
        context, attrs
    )

    override fun performClick(): Boolean {
        return allowClick() && super.performClick()
    }
}
