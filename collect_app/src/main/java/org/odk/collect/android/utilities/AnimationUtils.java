package org.odk.collect.android.utilities;

import androidx.core.view.animation.PathInterpolatorCompat;

import android.content.Context;
import android.provider.Settings;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Interpolator;
import android.view.animation.ScaleAnimation;
import android.view.animation.Transformation;

import org.odk.collect.android.listeners.Result;

/**
 * Created by Ing. Oscar G. Medina Cruz on 18/06/2016.
 */
public final class AnimationUtils {

    private static final Interpolator EASE_IN_OUT_QUART = PathInterpolatorCompat.create(0.77f, 0f, 0.175f, 1f);

    private AnimationUtils() {

    }

    public static void scaleInAnimation(final View view, int startOffset, int duration,
                                        Interpolator interpolator, final boolean isInvisible) {
        ScaleAnimation scaleInAnimation = new ScaleAnimation(0f, 1f, 0f, 1f, Animation.RELATIVE_TO_SELF,
                0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        scaleInAnimation.setInterpolator(interpolator);
        scaleInAnimation.setDuration(duration);
        scaleInAnimation.setStartOffset(startOffset);
        scaleInAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                if (isInvisible) {
                    view.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onAnimationEnd(Animation animation) {
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });
        view.startAnimation(scaleInAnimation);
    }

    // Added animation related code and inspiration from this Stack Overflow Question
    // https://stackoverflow.com/questions/4946295/android-expand-collapse-animation

    public static Animation expand(final View view, Result<Boolean> result) {
        int matchParentMeasureSpec = View.MeasureSpec.makeMeasureSpec(((View) view.getParent()).getWidth(), View.MeasureSpec.EXACTLY);
        int wrapContentMeasureSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        view.measure(matchParentMeasureSpec, wrapContentMeasureSpec);
        final int targetHeight = view.getMeasuredHeight();

        // Older versions of android (pre API 21) cancel animations for views with a height of 0 so use 1 instead.
        view.getLayoutParams().height = 1;
        view.setVisibility(View.VISIBLE);

        Animation animation = new Animation() {
            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t) {

                view.getLayoutParams().height = interpolatedTime == 1
                        ? ViewGroup.LayoutParams.WRAP_CONTENT
                        : (int) (targetHeight * interpolatedTime);

                view.requestLayout();
            }

            @Override
            public boolean willChangeBounds() {
                return true;
            }
        };

        animation.setInterpolator(EASE_IN_OUT_QUART);
        animation.setDuration(computeDurationFromHeight(view));
        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                //triggered when animation starts.
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                result.onComplete(true);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
                //triggered when animation repeats.
            }
        });
        view.startAnimation(animation);

        return animation;
    }

    public static Animation collapse(final View view, Result<Boolean> result) {
        final int initialHeight = view.getMeasuredHeight();

        Animation a = new Animation() {
            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t) {
                if (interpolatedTime == 1) {
                    view.setVisibility(View.GONE);
                } else {
                    view.getLayoutParams().height = initialHeight - (int) (initialHeight * interpolatedTime);
                    view.requestLayout();
                }
            }

            @Override
            public boolean willChangeBounds() {
                return true;
            }
        };

        a.setInterpolator(EASE_IN_OUT_QUART);

        int durationMillis = computeDurationFromHeight(view);
        a.setDuration(durationMillis);

        a.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                //triggered when animation starts.

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                result.onComplete(true);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
                //triggered when animation repeats.
            }
        });

        view.startAnimation(a);

        return a;
    }

    private static int computeDurationFromHeight(View view) {
        // 1dp/ms * multiplier
        return (int) (view.getMeasuredHeight() / view.getContext().getResources().getDisplayMetrics().density);
    }

    public static boolean areAnimationsEnabled(Context context) {
        float duration = Settings.System.getFloat(
                context.getContentResolver(),
                Settings.System.ANIMATOR_DURATION_SCALE, 1);
        float transition = Settings.System.getFloat(
                context.getContentResolver(),
                Settings.System.TRANSITION_ANIMATION_SCALE, 1);

        return duration != 0 && transition != 0;
    }
}