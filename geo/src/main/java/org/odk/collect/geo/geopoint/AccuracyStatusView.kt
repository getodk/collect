package org.odk.collect.geo.geopoint

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import androidx.core.content.withStyledAttributes
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
            binding.locationStatus.text = formatLocationStatus(it)
        }
    }

    private fun formatLocationStatus(accuracy: LocationAccuracy): String {
        val formattedValue = DecimalFormat("#.##").format(accuracy.value)
        return when (accuracy) {
            is LocationAccuracy.Unacceptable -> {
                context.getString(R.string.location_accuracy_unacceptable, formattedValue)
            }

            else -> context.getString(R.string.location_accuracy, formattedValue)
        }
    }
}
