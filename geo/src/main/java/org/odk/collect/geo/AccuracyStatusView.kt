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
        // If we're about to hide progress and show accuracy then start animating
        if (binding.progressBar.visibility == View.VISIBLE) {
            Animations.fadeInAndOut(
                view = binding.currentAccuracy,
                duration = 4000,
                minAlpha = 0.3f,
                maxAlpha = 1.0f
            )
        }

        binding.progressBar.visibility = View.GONE
        binding.currentAccuracy.visibility = View.VISIBLE
        binding.qualitative.visibility = View.VISIBLE
        binding.action.visibility = View.VISIBLE

        val (backgroundColor, textColor) = getBackgroundAndTextColor(accuracy)
        binding.root.background = ColorDrawable(backgroundColor)
        binding.title.setTextColor(textColor)
        binding.qualitative.setTextColor(textColor)
        binding.action.setTextColor(textColor)
        binding.currentAccuracy.setTextColor(textColor)

        binding.currentAccuracy.text = formatAccuracy(context, accuracy)

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

        binding.action.text = if (accuracy >= 100) {
            context.getString(R.string.unacceptable_accuracy_tip)
        } else {
            context.getString(R.string.please_wait_for_improved_accuracy)
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
