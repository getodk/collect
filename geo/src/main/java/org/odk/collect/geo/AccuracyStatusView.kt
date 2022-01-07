package org.odk.collect.geo

import android.content.Context
import android.graphics.drawable.ColorDrawable
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import org.odk.collect.androidshared.system.ContextUtils.getThemeAttributeValue
import org.odk.collect.androidshared.ui.Animations
import org.odk.collect.geo.GeoUtils.formatAccuracy
import org.odk.collect.geo.databinding.AccuracyStatusBinding

class AccuracyStatusView(context: Context, attrs: AttributeSet?) : FrameLayout(context, attrs) {

    constructor(context: Context) : this(context, null)

    var binding = AccuracyStatusBinding.inflate(LayoutInflater.from(context), this, true)
        private set

    fun setAccuracy(accuracy: Float, accuracyThreshold: Float) {
        binding.progressBar.visibility = View.GONE
        binding.currentAccuracy.visibility = View.VISIBLE
        binding.qualitative.visibility = View.VISIBLE
        binding.strength.visibility = View.VISIBLE

        val (backgroundColor, textColor) = getBackgroundAndTextColor(accuracy)
        binding.root.background = ColorDrawable(backgroundColor)
        binding.title.setTextColor(textColor)
        binding.qualitative.setTextColor(textColor)
        binding.currentAccuracy.setTextColor(textColor)

        animateAccuracyChange(accuracy)

        binding.qualitative.text = if (accuracy < 10) {
            context.getString(
                R.string.distance_from_accuracy_goal,
                formatAccuracy(context, accuracy - accuracyThreshold),
                formatAccuracy(context, accuracyThreshold)
            )
        } else if (accuracy >= 100) {
            context.getString(R.string.unacceptable_accuracy)
        } else {
            context.getString(R.string.poor_accuracy)
        }

        binding.strength.progress = when {
            accuracy > 100 -> 40
            accuracy > (accuracyThreshold + 5) -> 60
            else -> 80
        }
    }

    private fun animateAccuracyChange(accuracy: Float) {
        if (binding.currentAccuracy.text.isBlank()) {
            binding.currentAccuracy.text = formatAccuracy(context, accuracy)
        } else {
            Animations.createAlphaAnimation(
                view = binding.currentAccuracy,
                startValue = 1.0f,
                endValue = 0.1f,
                duration = 500
            ).onEnd {
                binding.currentAccuracy.text = formatAccuracy(context, accuracy)
            }.then(
                Animations.createAlphaAnimation(
                    view = binding.currentAccuracy,
                    startValue = 0.1f,
                    endValue = 1.0f,
                    duration = 2000
                )
            ).start()
        }
    }

    private fun getBackgroundAndTextColor(accuracy: Float): Pair<Int, Int> {
        return if (accuracy >= 100) {
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
}
