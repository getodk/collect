package org.odk.collect.androidshared.ui.multiclicksafe

import android.content.Context
import android.util.AttributeSet
import com.google.android.material.button.MaterialButton

class DoubleClickSafeMaterialButton @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : MaterialButton(context, attrs, defStyleAttr) {

    override fun performClick(): Boolean {
        return allowClick() && super.performClick()
    }

    /**
     * Use [MultiClickGuard] with a scope unique to this object (class name + hash).
     */
    private fun allowClick(): Boolean {
        val scope = javaClass.name + hashCode()
        return MultiClickGuard.allowClick(scope)
    }
}
