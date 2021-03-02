package org.odk.collect.android.audio;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.odk.collect.android.R;

import static org.odk.collect.android.utilities.ViewUtils.pxFromDp;

public class VolumeBar extends LinearLayout {

    private Integer lastAmplitude;
    private int pips;

    public VolumeBar(@NonNull Context context) {
        super(context);
        init();
    }

    public VolumeBar(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public VolumeBar(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        setOrientation(LinearLayout.HORIZONTAL);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);

        if (changed && getHeight() > 0) {
            int pipSize = pxFromDp(getContext(), 24);
            int marginSize = pxFromDp(getContext(), 4);
            pips = getWidth() / (pipSize + marginSize);

            this.removeAllViews();
            for (int i = 0; i < pips; i++) {
                View pip = new View(getContext());

                LayoutParams layoutParams = new LayoutParams(pipSize, getHeight());
                if (i != pips - 1) {
                    layoutParams.setMarginEnd(marginSize);
                } else {
                    layoutParams.setMarginEnd(0);
                }

                pip.setLayoutParams(layoutParams);
                addView(pip);
            }
        }
    }

    public void addAmplitude(int amplitude) {
        lastAmplitude = amplitude;

        if (pips > 0) {
            int segmentAmplitude = 22760 / pips;
            int segmentsToFill = amplitude * 6 / segmentAmplitude;

            int filledColor = getContext().getResources().getColor(R.color.blue_500);
            int unfilledColor = getContext().getResources().getColor(R.color.gray600);

            for (int i = 0; i < pips; i++) {
                if (i < segmentsToFill) {
                    getChildAt(i).setBackground(new ColorDrawable(filledColor));
                } else {
                    getChildAt(i).setBackground(new ColorDrawable(unfilledColor));
                }
            }
        }
    }

    @Nullable
    public Integer getLatestAmplitude() {
        return lastAmplitude;
    }
}
