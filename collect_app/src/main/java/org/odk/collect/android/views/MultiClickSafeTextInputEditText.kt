package org.odk.collect.android.views

import android.content.Context
import android.util.AttributeSet
import org.odk.collect.android.utilities.MultiClickGuard.allowClick
import com.google.android.material.textfield.TextInputEditText

class MultiClickSafeTextInputEditText : TextInputEditText {
    constructor(context: Context) : super(context)

    constructor(context: Context?, attrs: AttributeSet?) : super(
        context!!, attrs
    )

    override fun performClick(): Boolean {
        return allowClick(javaClass.name) && super.performClick()
    }
}
