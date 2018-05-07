package org.odk.collect.android.views;

import android.animation.ValueAnimator;
import android.content.Context;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import org.odk.collect.android.R;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ProgressBar extends FrameLayout {

    private static final int DURATION_MILLIS = 1000;
    @BindView(R.id.progress_view)
    View progressBar;
    @BindView(R.id.divider)
    View divider;

    public ProgressBar(@NonNull Context context) {
        this(context, null);
    }

    public ProgressBar(@NonNull Context context, @NonNull AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ProgressBar(@NonNull Context context, @NonNull AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        if (!isInEditMode()) {
            init(context, attrs, defStyleAttr);
        }
    }

    private void init(Context context, AttributeSet attrs, int defStyleAttr) {
        View view = LayoutInflater.from(getContext()).inflate(R.layout.progress_bar_view, this, true);
        ButterKnife.apply(view);
    }

    public void setProgressPercent(int progress) {
        int endWith = (progress / 100) * divider.getMeasuredWidth();

        ValueAnimator anim = ValueAnimator.ofInt(progressBar.getMeasuredWidth(), endWith);
        anim.addUpdateListener(valueAnimator -> {
            int val = (Integer) valueAnimator.getAnimatedValue();
            ViewGroup.LayoutParams layoutParams = progressBar.getLayoutParams();
            layoutParams.width = val;
            progressBar.setLayoutParams(layoutParams);
        });
        anim.setDuration(DURATION_MILLIS);
        anim.start();
    }
}
