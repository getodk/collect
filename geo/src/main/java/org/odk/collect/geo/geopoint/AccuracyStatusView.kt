package org.odk.collect.geo.geopoint

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import androidx.core.content.withStyledAttributes
import org.odk.collect.androidshared.system.ContextUtils.getThemeAttributeValue
import org.odk.collect.geo.databinding.AccuracyStatusLayoutBinding
import org.odk.collect.strings.R
import java.text.DecimalFormat

internal class AccuracyStatusView(context: Context, attrs: AttributeSet?) :
    FrameLayout(context, attrs) {

    constructor(context: Context) : this(context, null)

    val binding =
        AccuracyStatusLayoutBinding.inflate(LayoutInflater.from(context), this, true)

    var title: String = ""
        set(value) {
            field = value
            render()
        }

    var accuracy: LocationAccuracy? = null
        set(value) {
            field = value
            render()
        }

    init {
        context.withStyledAttributes(attrs, org.odk.collect.geo.R.styleable.AccuracyStatusView) {
            title = getString(org.odk.collect.geo.R.styleable.AccuracyStatusView_title) ?: ""
        }
    }

    private fun render() {
        binding.title.text = title
        if (title.isBlank()) {
            binding.title.visibility = View.GONE
        } else {
            binding.title.visibility = View.VISIBLE
        }

        accuracy?.let {
            binding.locationStatus.text = formatLocationStatus(it.value)

            if (accuracy is LocationAccuracy.Unacceptable) {
                setBackgroundColor(
                    getThemeAttributeValue(
                        context,
                        com.google.android.material.R.attr.colorError
                    )
                )

                val colorOnError = getThemeAttributeValue(
                    context,
                    com.google.android.material.R.attr.colorOnError
                )

                binding.title.setTextColor(colorOnError)
                binding.locationStatus.setTextColor(colorOnError)
            } else {
                setBackgroundColor(
                    getThemeAttributeValue(
                        context,
                        com.google.android.material.R.attr.colorSurface
                    )
                )

                val colorOnSurface = getThemeAttributeValue(
                    context,
                    com.google.android.material.R.attr.colorOnSurface
                )

                binding.title.setTextColor(colorOnSurface)
                binding.locationStatus.setTextColor(colorOnSurface)
            }
        }
    }

    private fun formatLocationStatus(accuracyRadius: Float): String {
        return context.getString(
            R.string.location_accuracy,
            DecimalFormat("#.##").format(accuracyRadius)
        )
    }
}
