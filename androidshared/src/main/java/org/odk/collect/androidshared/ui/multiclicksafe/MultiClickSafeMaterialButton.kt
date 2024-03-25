package org.odk.collect.androidshared.ui.multiclicksafe

import android.content.Context
import android.util.AttributeSet
import com.google.android.material.button.MaterialButton
import org.odk.collect.androidshared.ui.multiclicksafe.MultiClickGuard.allowClick

open class MultiClickSafeMaterialButton : MaterialButton {
    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet?) : super(
        context,
        attrs
    )

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    override fun performClick(): Boolean {
        return allowClick() && super.performClick()
    }
}
