package org.odk.collect.material

import android.content.Context
import android.util.AttributeSet
import android.view.View
import com.google.android.material.color.MaterialColors
import org.odk.collect.androidshared.system.ContextUtils

class ErrorsPill(context: Context, attrs: AttributeSet?) : MaterialPill(context, attrs) {
    fun setup(state: State) {
        when (state) {
            State.ERRORS -> {
                visibility = View.VISIBLE
                setIcon(org.odk.collect.icons.R.drawable.ic_baseline_rule_24)
                setText(org.odk.collect.strings.R.string.draft_errors)
                setPillBackgroundColor(MaterialColors.getColor(this, com.google.android.material.R.attr.colorErrorContainer))
                setTextColor(ContextUtils.getThemeAttributeValue(context, com.google.android.material.R.attr.colorOnErrorContainer))
                setIconTint(ContextUtils.getThemeAttributeValue(context, com.google.android.material.R.attr.colorOnErrorContainer))
            }
            State.NO_ERRORS -> {
                visibility = View.VISIBLE
                setIcon(org.odk.collect.icons.R.drawable.ic_baseline_check_24)
                setText(org.odk.collect.strings.R.string.draft_no_errors)
                setPillBackgroundColor(MaterialColors.getColor(this, com.google.android.material.R.attr.colorSurfaceContainerHighest))
                setTextColor(ContextUtils.getThemeAttributeValue(context, com.google.android.material.R.attr.colorOnSurface))
                setIconTint(ContextUtils.getThemeAttributeValue(context, com.google.android.material.R.attr.colorOnSurface))
            }
        }
    }

    enum class State {
        ERRORS,
        NO_ERRORS
    }
}
