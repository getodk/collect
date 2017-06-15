package org.odk.collect.android.utilities;

import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Interpolator;
import android.view.animation.ScaleAnimation;

/**
 * Created by Ing. Oscar G. Medina Cruz on 18/06/2016.
 */
public class AnimateUtils {
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
}