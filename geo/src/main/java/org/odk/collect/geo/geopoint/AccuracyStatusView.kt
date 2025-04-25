package org.odk.collect.geo.geopoint

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import androidx.core.content.withStyledAttributes
import androidx.core.view.isGone
import org.odk.collect.geo.GeoUtils
import org.odk.collect.geo.databinding.AccuracyStatusLayoutBinding
import org.odk.collect.strings.R

internal class AccuracyStatusView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : FrameLayout(context, attrs, defStyle) {

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
        binding.title.isGone = title.isBlank()

        accuracy?.let {
            binding.locationStatus.text = formatLocationStatus(it)
        }
    }

    private fun formatLocationStatus(accuracy: LocationAccuracy): String {
        val formattedAccuracy = GeoUtils.formatAccuracy(context, accuracy.value)
        return when (accuracy) {
            is LocationAccuracy.Unacceptable -> {
                context.getString(R.string.location_accuracy_unacceptable, formattedAccuracy)
            }

            else -> context.getString(R.string.location_accuracy, formattedAccuracy)
        }
    }
}
