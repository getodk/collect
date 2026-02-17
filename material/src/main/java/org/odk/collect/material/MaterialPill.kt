package org.odk.collect.material

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.graphics.Color
import androidx.core.content.withStyledAttributes
import org.odk.collect.androidshared.system.ContextUtils.getThemeAttributeValue
import org.odk.collect.androidshared.ui.ComposeThemeProvider.Companion.setContextThemedContent
import org.odk.collect.material.databinding.PillBinding

/**
 * [android.view.View] wrapper for [Pill]
 */
open class MaterialPill(context: Context, attrs: AttributeSet?) :
    FrameLayout(context, attrs) {

    var text: String = ""
        set(value) {
            field = value
            render()
        }

    @DrawableRes
    private var icon: Int? = null

    @ColorInt
    private var iconTint: Int? = null

    @ColorInt
    private var backgroundColor: Int? = null

    @ColorInt
    private var textColor: Int? = null

    val binding = PillBinding.inflate(LayoutInflater.from(context), this, true)

    init {
        context.withStyledAttributes(attrs, R.styleable.MaterialPill) {
            text = getString(R.styleable.MaterialPill_text) ?: ""

            val iconId = getResourceId(R.styleable.MaterialPill_icon, -1)
            if (iconId != -1) {
                setIcon(iconId)
            }

            val backgroundColor = getColor(
                R.styleable.MaterialPill_pillBackgroundColor,
                getDefaultBackgroundColor(context)
            )

            setPillBackgroundColor(backgroundColor)
        }

        render()
    }

    fun setText(@StringRes id: Int) {
        text = context.getString(id)
        render()
    }

    fun setIcon(@DrawableRes id: Int) {
        icon = id
        render()
    }

    fun setIconTint(@ColorInt color: Int) {
        iconTint = color
        render()
    }

    fun setPillBackgroundColor(@ColorInt color: Int) {
        backgroundColor = color
        render()
    }

    fun setTextColor(@ColorInt color: Int) {
        textColor = color
        render()
    }

    private fun getDefaultBackgroundColor(context: Context) = getThemeAttributeValue(
        context,
        com.google.android.material.R.attr.colorPrimaryContainer
    )

    private fun render() {
        binding.composeView.setContextThemedContent {
            val backgroundColor =
                backgroundColor?.let { Color(it) } ?: MaterialTheme.colorScheme.primaryContainer
            val iconTint =
                iconTint?.let { Color(it) } ?: MaterialTheme.colorScheme.onPrimaryContainer
            val textColor =
                textColor?.let { Color(it) } ?: MaterialTheme.colorScheme.onPrimaryContainer

            Pill(
                text = text,
                icon = icon,
                backgroundColor = backgroundColor,
                textColor = iconTint,
                iconColor = textColor
            )
        }
    }
}
