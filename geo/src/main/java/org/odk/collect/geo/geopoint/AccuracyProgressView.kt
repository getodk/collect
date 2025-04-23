package org.odk.collect.geo.geopoint

import android.content.Context
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import org.odk.collect.androidshared.system.ContextUtils.getThemeAttributeValue
import org.odk.collect.geo.GeoUtils.formatAccuracy
import org.odk.collect.geo.databinding.AccuracyProgressLayoutBinding

internal class AccuracyProgressView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : FrameLayout(context, attrs, defStyle) {

    val binding = AccuracyProgressLayoutBinding.inflate(LayoutInflater.from(context), this, true)

    var accuracy: LocationAccuracy? = null
        set(value) {
            field = value
            if (value != null) {
                render(value)
            }
        }

    private fun render(accuracy: LocationAccuracy) {
        val (backgroundColor, textColor) = getBackgroundAndTextColor(accuracy)
        binding.root.background = ColorDrawable(backgroundColor)
        binding.title.setTextColor(textColor)
        binding.text.setTextColor(textColor)
        binding.currentAccuracy.setTextColor(textColor)
        binding.strength.setIndicatorColor(textColor)

        binding.currentAccuracy.text = formatAccuracy(context, accuracy.value)

        val (text, strength) = getTextAndStrength(accuracy)
        binding.text.setText(text)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            binding.strength.setProgress(strength, true)
        } else {
            binding.strength.progress = strength
        }
    }

    private fun getBackgroundAndTextColor(accuracy: LocationAccuracy): Pair<Int, Int> {
        return if (accuracy is LocationAccuracy.Unacceptable) {
            Pair(
                getThemeAttributeValue(context, com.google.android.material.R.attr.colorError),
                getThemeAttributeValue(context, com.google.android.material.R.attr.colorOnError)
            )
        } else {
            Pair(
                getThemeAttributeValue(context, com.google.android.material.R.attr.colorPrimary),
                getThemeAttributeValue(context, com.google.android.material.R.attr.colorOnPrimary)
            )
        }
    }

    private fun getTextAndStrength(accuracy: LocationAccuracy): Pair<Int, Int> {
        return when (accuracy) {
            is LocationAccuracy.Improving -> Pair(org.odk.collect.strings.R.string.improving_accuracy, 80)
            is LocationAccuracy.Poor -> Pair(org.odk.collect.strings.R.string.poor_accuracy, 60)
            is LocationAccuracy.Unacceptable -> Pair(org.odk.collect.strings.R.string.unacceptable_accuracy, 40)
        }
    }
}
