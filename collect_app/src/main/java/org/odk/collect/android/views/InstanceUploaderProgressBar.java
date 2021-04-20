package org.odk.collect.android.views;

import android.animation.ValueAnimator;
import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;

import org.odk.collect.android.R;

import butterknife.BindView;
import butterknife.ButterKnife;

public class InstanceUploaderProgressBar extends FrameLayout {

    private static final int DURATION_MILLIS = 1000;
    @BindView(R.id.progress_view)
    View progressBar;
    @BindView(R.id.divider)
    View divider;
    @BindView(R.id.main_layout)
    FrameLayout mainLayout;

    public InstanceUploaderProgressBar(Context context) {
        this(context, null);
    }

    public InstanceUploaderProgressBar(@NonNull Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public InstanceUploaderProgressBar(@NonNull Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        if (!isInEditMode()) {
            init();
        }
    }

    private void init() {
        View view = LayoutInflater.from(getContext()).inflate(R.layout.progress_bar_view, this, true);
        ButterKnife.bind(this, view);
    }

    public void setProgressPercent(int progress, boolean animate) {
        mainLayout.post(() -> {
            int progressWidth = (progress / 100) * divider.getMeasuredWidth();

            if (animate) {
                ValueAnimator anim = ValueAnimator.ofInt(progressBar.getMeasuredWidth(), progressWidth);
                anim.addUpdateListener(valueAnimator -> {
                    int val = (Integer) valueAnimator.getAnimatedValue();
                    ViewGroup.LayoutParams layoutParams = progressBar.getLayoutParams();
                    layoutParams.width = val;
                    progressBar.setLayoutParams(layoutParams);
                });
                anim.setDuration(DURATION_MILLIS);
                anim.start();
            } else {
                ViewGroup.LayoutParams layoutParams = progressBar.getLayoutParams();
                layoutParams.width = progressWidth;
                progressBar.setLayoutParams(layoutParams);
            }
        });
    }
}
