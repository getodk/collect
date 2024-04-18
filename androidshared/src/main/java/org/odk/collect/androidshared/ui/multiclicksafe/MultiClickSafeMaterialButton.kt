package org.odk.collect.androidshared.ui.multiclicksafe

import android.content.Context
import android.util.AttributeSet
import com.google.android.material.button.MaterialButton
import org.odk.collect.androidshared.R
import org.odk.collect.androidshared.ui.multiclicksafe.MultiClickGuard.allowClick

open class MultiClickSafeMaterialButton @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : MaterialButton(context, attrs, defStyleAttr) {
    private val screenName: String

    init {
        context.theme.obtainStyledAttributes(
            attrs,
            R.styleable.MultiClickSafeMaterialButton,
            0,
            0
        ).apply {
            try {
                screenName = this.getString(R.styleable.MultiClickSafeMaterialButton_screenName) ?: javaClass.name
            } finally {
                recycle()
            }
        }
    }

    override fun performClick(): Boolean {
        return allowClick(screenName) && super.performClick()
    }
}
