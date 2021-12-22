package org.odk.collect.androidshared.ui

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ValueAnimator
import android.view.View
import org.odk.collect.androidshared.ui.Animations.DISABLE_ANIMATIONS

/**
 * Helpers for running common animations. These are "test safe" in that animations can be disabled
 * using [DISABLE_ANIMATIONS] - this should be set to `true` in Robolectric tests to avoid
 * infinite loops.
 */
object Animations {

    var DISABLE_ANIMATIONS = false

    fun fadeInAndOut(view: View, duration: Int, minAlpha: Float, maxAlpha: Float) {
        loopSequentially(
            createAlphaAnimation(
                view = view,
                startValue = maxAlpha,
                endValue = minAlpha,
                duration = duration
            ),
            createAlphaAnimation(
                view = view,
                startValue = minAlpha,
                endValue = maxAlpha,
                duration = duration
            )
        )
    }

    private fun createAlphaAnimation(
        view: View,
        startValue: Float,
        endValue: Float,
        duration: Int
    ): ValueAnimator {
        val outAnimation = ValueAnimator.ofFloat(startValue, endValue)
        outAnimation.duration = duration / 2L
        outAnimation.addUpdateListener {
            view.alpha = it.animatedValue as Float
        }

        return outAnimation
    }

    private fun loopSequentially(vararg animators: Animator) {
        val animationLoop = AnimatorSet().also {
            it.playSequentially(*animators)
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

        if (!DISABLE_ANIMATIONS) {
            animationLoop.start()
        }
    }
}
