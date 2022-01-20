package org.odk.collect.geo

import android.content.Context
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import org.odk.collect.androidshared.system.ContextUtils.getThemeAttributeValue
import org.odk.collect.geo.GeoUtils.formatAccuracy
import org.odk.collect.geo.databinding.AccuracyStatusBinding

internal class AccuracyStatusView(context: Context, attrs: AttributeSet?) :
    FrameLayout(context, attrs) {

    constructor(context: Context) : this(context, null)

    var binding = AccuracyStatusBinding.inflate(LayoutInflater.from(context), this, true)
        private set

    var accuracy: GeoPointAccuracy? = null
        set(value) {
            field = value
            if (value != null) {
                render(value)
            }
        }

    private fun render(accuracy: GeoPointAccuracy) {
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

    private fun getBackgroundAndTextColor(accuracy: GeoPointAccuracy): Pair<Int, Int> {
        return if (accuracy is GeoPointAccuracy.Unacceptable) {
            Pair(
                getThemeAttributeValue(context, R.attr.colorError),
                getThemeAttributeValue(context, R.attr.colorOnError)
            )
        } else {
            Pair(
                getThemeAttributeValue(context, R.attr.colorPrimary),
                getThemeAttributeValue(context, R.attr.colorOnPrimary)
            )
        }
    }

    private fun getTextAndStrength(accuracy: GeoPointAccuracy): Pair<Int, Int> {
        return when (accuracy) {
            is GeoPointAccuracy.Improving -> Pair(R.string.improving_accuracy, 80)
            is GeoPointAccuracy.Poor -> Pair(R.string.poor_accuracy, 60)
            is GeoPointAccuracy.Unacceptable -> Pair(R.string.unacceptable_accuracy, 40)
        }
    }
}
