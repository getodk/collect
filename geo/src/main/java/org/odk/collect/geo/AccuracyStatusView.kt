package org.odk.collect.geo

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.drawable.ColorDrawable
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import org.odk.collect.androidshared.system.ContextUtils.getThemeAttributeValue
import org.odk.collect.geo.GeoUtils.formatAccuracy
import org.odk.collect.geo.databinding.AccuracyStatusBinding

class AccuracyStatusView(context: Context, attrs: AttributeSet?) : FrameLayout(context, attrs) {

    constructor(context: Context) : this(context, null)

    var binding = AccuracyStatusBinding.inflate(LayoutInflater.from(context), this, true)
        private set

    fun setAccuracy(accuracy: Float, accuracyThreshold: Float) {
        // If we're about to hide progress and show accuracy then start animating
        if (binding.progressBar.visibility == View.VISIBLE) {
            animateCurrentAccuracy()
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

        binding.currentAccuracy.text = formatAccuracy(accuracy)

        binding.qualitative.text = if (accuracy < 10) {
            context.getString(
                R.string.distance_from_accuracy_goal,
                formatAccuracy(accuracy - accuracyThreshold),
                formatAccuracy(accuracyThreshold)
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

    private fun animateCurrentAccuracy() {
        val outAnimation = ValueAnimator.ofFloat(1.0f, 0.3f).also {
            it.duration = 2000
            it.addUpdateListener {
                binding.currentAccuracy.alpha = it.animatedValue as Float
            }
        }

        val inAnimation = ValueAnimator.ofFloat(0.3f, 1.0f).also {
            it.duration = 2000
            it.addUpdateListener {
                binding.currentAccuracy.alpha = it.animatedValue as Float
            }
        }

        val animationLoop = AnimatorSet().also {
            it.playSequentially(outAnimation, inAnimation)
            it.addListener(object : Animator.AnimatorListener {
                override fun onAnimationStart(animation: Animator?) {
                }

                override fun onAnimationEnd(animation: Animator?) {
                    animation?.start()
                }

                override fun onAnimationCancel(animation: Animator?) {
                }

                override fun onAnimationRepeat(animation: Animator?) {
                }
            })
        }

        if (Constants.ANIMATED) {
            animationLoop.start()
        }
    }
}
