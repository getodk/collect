package org.odk.collect.material

import android.content.Context
import android.content.res.ColorStateList
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.core.content.res.ResourcesCompat
import androidx.core.content.withStyledAttributes
import com.google.android.material.shape.MaterialShapeDrawable
import com.google.android.material.shape.ShapeAppearanceModel
import org.odk.collect.androidshared.system.ContextUtils.getThemeAttributeValue
import org.odk.collect.material.databinding.PillBinding

/**
 * Implementation of "pills" present on the Material 3 website and its examples, but not
 * included in the spec or in Android's MaterialComponents. The pill will use the
 * `?shapeAppearanceCornerSmall` shape appearance for the current theme.
 */
open class MaterialPill(context: Context, attrs: AttributeSet?) :
    FrameLayout(context, attrs) {

    var text: String? = null
        set(value) {
            field = value
            binding.text.text = text
        }

    private val shapeAppearanceModel =
        ShapeAppearanceModel.builder(context, getShapeAppearance(context), -1).build()

    val binding = PillBinding.inflate(LayoutInflater.from(context), this, true)

    init {
        background = createMaterialShapeDrawable(getDefaultBackgroundColor(context))

        context.withStyledAttributes(attrs, R.styleable.MaterialPill) {
            text = getString(R.styleable.MaterialPill_text)
            setIcon(getResourceId(R.styleable.MaterialPill_icon, 0))
        }
    }

    fun setText(@StringRes id: Int) {
        text = context.getString(id)
    }

    fun setIcon(@DrawableRes id: Int) {
        val drawable = ResourcesCompat.getDrawable(resources, id, context.theme)
        binding.icon.setImageDrawable(drawable)
    }

    fun setIconTint(@ColorInt color: Int) {
        binding.icon.setColorFilter(color)
    }

    fun setPillBackgroundColor(@ColorInt color: Int) {
        background = createMaterialShapeDrawable(color)
    }

    fun setTextColor(@ColorInt color: Int) {
        binding.text.setTextColor(color)
    }

    private fun getShapeAppearance(context: Context) = getThemeAttributeValue(
        context,
        com.google.android.material.R.attr.shapeAppearanceCornerSmall
    )

    private fun getDefaultBackgroundColor(context: Context) = getThemeAttributeValue(
        context,
        com.google.android.material.R.attr.colorPrimaryContainer
    )

    private fun createMaterialShapeDrawable(@ColorInt color: Int): MaterialShapeDrawable {
        return MaterialShapeDrawable(shapeAppearanceModel).also {
            it.fillColor = ColorStateList.valueOf(color)
        }
    }
}
