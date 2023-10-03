package org.odk.collect.material

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.core.content.res.ResourcesCompat

class MaterialPill(context: Context, attrs: AttributeSet?) : FrameLayout(context, attrs) {

    init {
        inflate(context, R.layout.pill, this)
    }

    fun setText(@StringRes id: Int) {
        findViewById<TextView>(R.id.text).setText(id)
    }

    fun setIcon(@DrawableRes id: Int) {
        val drawable = ResourcesCompat.getDrawable(resources, id, context.theme)
        findViewById<ImageView>(R.id.icon).setImageDrawable(drawable)
    }
}
