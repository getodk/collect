package org.odk.collect.android.audio;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.jetbrains.annotations.NotNull;
import org.odk.collect.android.R;

import static androidx.core.content.res.ResourcesCompat.getDrawable;
import static org.odk.collect.android.utilities.ViewUtils.dpFromPx;
import static org.odk.collect.android.utilities.ViewUtils.pxFromDp;

public class VolumeBar extends LinearLayout {

    public static final int MAX_AMPLITUDE = 22760;

    private Integer lastAmplitude;
    private int pips;

    private Drawable filledBackground;
    private Drawable unfilledBackground;
    private LayoutParams pipLayoutParams;

    public VolumeBar(@NonNull Context context) {
        super(context);
        init(context);
    }

    public VolumeBar(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public VolumeBar(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        setOrientation(LinearLayout.HORIZONTAL);

        // Setup objects used during drawing/rendering amplitude
        pipLayoutParams = new LayoutParams(0, 0);
        filledBackground = getDrawable(context.getResources(), R.drawable.pill_filled, context.getTheme());
        unfilledBackground = getDrawable(context.getResources(), R.drawable.pill_unfilled, context.getTheme());
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);

        if (changed && getHeight() > 0) {
            int pipSize = getBestPipSize(getWidth());
            int marginSize = pxFromDp(getContext(), 4);

            pips = (getWidth() + marginSize) / (pipSize + marginSize);

            this.removeAllViews();
            for (int i = 0; i < pips; i++) {
                View pip = createPipView(pipSize, marginSize, i != pips - 1);
                addView(pip);
            }
        }
    }

    public void addAmplitude(int amplitude) {
        lastAmplitude = amplitude;

        if (pips > 0) {
            int segmentAmplitude = MAX_AMPLITUDE / pips;
            int adjustedAmplitude = amplitude * 6;
            int segmentsToFill = adjustedAmplitude / segmentAmplitude;

            for (int i = 0; i < pips; i++) {
                if (i < segmentsToFill) {
                    getChildAt(i).setBackground(filledBackground);
                } else {
                    getChildAt(i).setBackground(unfilledBackground);
                }
            }
        }
    }

    @Nullable
    public Integer getLatestAmplitude() {
        return lastAmplitude;
    }

    private int getBestPipSize(int width) {
        if (dpFromPx(getContext(), width) >= 164) {
            return pxFromDp(getContext(), 24);
        } else {
            return pxFromDp(getContext(), 20);
        }
    }

    @NotNull
    private View createPipView(int pipSize, int marginSize, boolean isLast) {
        View pip = new View(getContext());

        pipLayoutParams.width = pipSize;
        pipLayoutParams.height = getHeight();

        if (!isLast) {
            pipLayoutParams.setMarginEnd(marginSize);
        } else {
            pipLayoutParams.setMarginEnd(0);
        }

        pip.setLayoutParams(pipLayoutParams);
        return pip;
    }
}
