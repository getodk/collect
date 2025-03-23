package org.odk.collect.geo.geopoint

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import androidx.core.content.withStyledAttributes
import org.odk.collect.geo.GeoUtils
import org.odk.collect.geo.databinding.AccuracyStatusLayoutBinding
import org.odk.collect.strings.R
import java.text.DecimalFormat

internal class AccuracyStatusView(context: Context, attrs: AttributeSet?) :
    FrameLayout(context, attrs) {

    private val binding =
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

        accuracy?.let {
            binding.locationStatus.text = formatLocationStatus(it.provider, it.value)
        }
    }

    private fun formatLocationStatus(provider: String?, accuracyRadius: Float): String {
        return context.getString(
            R.string.location_accuracy,
            DecimalFormat("#.##").format(accuracyRadius)
        ) + ", " + context.getString(
            R.string.location_provider, GeoUtils.capitalizeGps(provider)
        )
    }
}
