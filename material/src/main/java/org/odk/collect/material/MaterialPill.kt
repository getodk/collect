package org.odk.collect.material

import android.content.Context
import android.content.res.ColorStateList
import android.util.AttributeSet
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.core.content.res.ResourcesCompat
import com.google.android.material.shape.MaterialShapeDrawable
import com.google.android.material.shape.ShapeAppearanceModel
import org.odk.collect.androidshared.system.ContextUtils.getThemeAttributeValue

/**
 * Implementation of "pills" present on the Material 3 website and its examples, but not
 * included in the spec or in Android's MaterialComponents. The pill will use the
 * `?shapeAppearanceCornerSmall` shape appearance for the current theme.
 */
class MaterialPill(context: Context, attrs: AttributeSet?) :
    FrameLayout(context, attrs) {

    private val shapeAppearanceModel =
        ShapeAppearanceModel.builder(context, getShapeAppearance(context), -1).build()

    init {
        inflate(context, R.layout.pill, this)
        background = createMaterialShapeDrawable(getDefaultBackgroundColor(context))
    }

    fun setText(@StringRes id: Int) {
        findViewById<TextView>(R.id.text).setText(id)
    }

    fun setIcon(@DrawableRes id: Int) {
        val drawable = ResourcesCompat.getDrawable(resources, id, context.theme)
        findViewById<ImageView>(R.id.icon).setImageDrawable(drawable)
    }

    fun setIconTint(@ColorInt color: Int) {
        findViewById<ImageView>(R.id.icon).setColorFilter(color)
    }

    fun setPillBackgroundColor(@ColorInt color: Int) {
        background = createMaterialShapeDrawable(color)
    }

    fun setTextColor(@ColorInt color: Int) {
        findViewById<TextView>(R.id.text).setTextColor(color)
    }

    private fun getShapeAppearance(context: Context) = getThemeAttributeValue(
        context,
        com.google.android.material.R.attr.shapeAppearanceCornerSmall
    )

    private fun getDefaultBackgroundColor(context: Context) = getThemeAttributeValue(
        context,
        com.google.android.material.R.attr.colorPrimary
    )

    private fun createMaterialShapeDrawable(@ColorInt color: Int): MaterialShapeDrawable {
        return MaterialShapeDrawable(shapeAppearanceModel).also {
            it.fillColor = ColorStateList.valueOf(color)
        }
    }
}
