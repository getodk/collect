package org.odk.collect.android.utilities;


import android.content.Context;
import android.support.annotation.AnimRes;
import android.support.annotation.NonNull;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

public class AnimationUtil {
    @NonNull
    private final Context context;

    public AnimationUtil(@NonNull Context context) {
        this.context = context;
    }

    public Animation loadAnimation(@AnimRes int id) {
        return AnimationUtils.loadAnimation(context, id);
    }
}
