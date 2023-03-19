package org.odk.collect.androidshared.ui.multiclicksafe

import android.content.Context
import android.util.AttributeSet
import com.google.android.material.button.MaterialButton
import org.odk.collect.androidshared.ui.multiclicksafe.MultiClickGuard.allowClick

class MultiClickSafeButton : MaterialButton {
    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet?) : super(
        context, attrs
    )

    override fun performClick(): Boolean {
        return allowClick() && super.performClick()
    }
}
