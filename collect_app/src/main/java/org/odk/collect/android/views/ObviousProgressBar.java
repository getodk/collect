package org.odk.collect.android.views;

import android.content.Context;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;

/**
 * A progress bar that shows for a minimum amount fo time so it's obvious to the user that
 * something has happened.
 */
public class ObviousProgressBar extends ProgressBar {

    public static final int MINIMUM_SHOW_TIME = 750;

    private final Handler handler;

    private Long shownAt;

    public ObviousProgressBar(Context context) {
        super(context);
        handler = new Handler();
    }

    public ObviousProgressBar(@NonNull Context context, AttributeSet attrs) {
        super(context, attrs);
        handler = new Handler();
    }

    public ObviousProgressBar(@NonNull Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        handler = new Handler();
    }

    public void show() {
        handler.removeCallbacksAndMessages(null);
        shownAt = System.currentTimeMillis();
        super.setVisibility(View.VISIBLE);
    }

    public void hide(int visibility) {
        if (shownAt != null) {
            long timeShown = System.currentTimeMillis() - shownAt;

            if (timeShown < MINIMUM_SHOW_TIME) {
                long delay = MINIMUM_SHOW_TIME - timeShown;

                handler.removeCallbacksAndMessages(null);
                handler.postDelayed(() -> makeHiddenOrGone(visibility), delay);
            } else {
                makeHiddenOrGone(visibility);
            }
        } else {
            makeHiddenOrGone(visibility);
        }
    }

    private void makeHiddenOrGone(int visibility) {
        super.setVisibility(visibility);
        shownAt = null;
    }
}
