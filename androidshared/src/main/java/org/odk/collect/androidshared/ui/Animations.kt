package org.odk.collect.androidshared.ui

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ValueAnimator
import android.view.View
import org.odk.collect.androidshared.ui.Animations.DISABLE_ANIMATIONS

/**
 * Helpers/extensions for running animations. These are "test safe" in that animations can be disabled
 * using [DISABLE_ANIMATIONS] - this should be set to `true` in Robolectric tests to avoid
 * infinite loops.
 */
object Animations {

    var DISABLE_ANIMATIONS = false

    fun createAlphaAnimation(
        view: View,
        startValue: Float,
        endValue: Float,
        duration: Long
    ): DisableableAnimatorWrapper {
        val animation = ValueAnimator.ofFloat(startValue, endValue)
        animation.duration = duration
        animation.addUpdateListener {
            view.alpha = it.animatedValue as Float
        }

        return DisableableAnimatorWrapper(animation)
    }
}

class DisableableAnimatorWrapper(private val wrapped: Animator) {

    fun onEnd(onEnd: () -> Unit): DisableableAnimatorWrapper {
        wrapped.addListener(object : Animator.AnimatorListener {
            override fun onAnimationStart(animation: Animator?) {
            }

            override fun onAnimationEnd(animation: Animator?) {
                onEnd()
            }

            override fun onAnimationCancel(animation: Animator?) {
            }

            override fun onAnimationRepeat(animation: Animator?) {
            }
        })

        return this
    }

    fun then(other: DisableableAnimatorWrapper): DisableableAnimatorWrapper {
        val set = AnimatorSet()
        set.playSequentially(this.wrapped, other.wrapped)

        return DisableableAnimatorWrapper(set)
    }

    fun start() {
        if (!DISABLE_ANIMATIONS) {
            wrapped.start()
        } else {
            // Just run listeners immediately if we're not running the actual animations
            if (wrapped is AnimatorSet) {
                (wrapped.childAnimations + wrapped).forEach { anim ->
                    anim.listeners?.forEach {
                        it.onAnimationStart(wrapped)
                        it.onAnimationEnd(wrapped)
                    }
                }
            } else {
                wrapped.listeners?.forEach {
                    it.onAnimationStart(wrapped)
                    it.onAnimationEnd(wrapped)
                }
            }
        }
    }
}
