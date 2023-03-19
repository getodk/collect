package org.odk.collect.android.listeners;

import android.content.Context;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.widget.NestedScrollView;

import org.odk.collect.android.utilities.FlingRegister;
import org.odk.collect.androidshared.utils.ScreenUtils;
import org.odk.collect.settings.keys.ProjectKeys;
import org.odk.collect.shared.settings.Settings;

public class SwipeHandler {

    private final GestureDetector gestureDetector;
    private final OnSwipeListener onSwipe;
    private View view;
    private boolean allowSwiping = true;
    private boolean beenSwiped;
    private final Settings generalSettings;

    public interface OnSwipeListener {
        void onSwipeBackward();
        void onSwipeForward();
    }

    public SwipeHandler(Context context, Settings generalSettings) {
        gestureDetector = new GestureDetector(context, new GestureListener());
        this.onSwipe = (OnSwipeListener) context;
        this.generalSettings = generalSettings;
    }

    public void setView(View view) {
        this.view = view;
    }

    public void setAllowSwiping(boolean allowSwiping) {
        this.allowSwiping = allowSwiping;
    }

    public void setBeenSwiped(boolean beenSwiped) {
        this.beenSwiped = beenSwiped;
    }

    public boolean beenSwiped() {
        return beenSwiped;
    }

    public GestureDetector getGestureDetector() {
        return gestureDetector;
    }

    public class GestureListener implements GestureDetector.OnGestureListener {

        @Override
        public boolean onDown(MotionEvent event) {
            return false;
        }

        @Override
        public void onShowPress(MotionEvent e) {

        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            // The onFling() captures the 'up' event so our view thinks it gets long pressed. We don't want that, so cancel it.
            if (view != null) {
                view.cancelLongPress();
            }
            return false;
        }

        @Override
        public void onLongPress(MotionEvent e) {

        }

        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            return false;
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            if (view == null) {
                return false;
            }

            FlingRegister.flingDetected();

            if (e1 != null && e2 != null
                    && generalSettings.getString(ProjectKeys.KEY_NAVIGATION).contains(ProjectKeys.NAVIGATION_SWIPE)
                    && allowSwiping) {
                // Looks for user swipes. If the user has swiped, move to the appropriate screen.

                // For all screens a swipe is left/right of at least .25" and up/down of less than .25" OR left/right of > .5"
                int xpixellimit = (int) (ScreenUtils.xdpi(view.getContext()) * .25);
                int ypixellimit = (int) (ScreenUtils.ydpi(view.getContext()) * .25);

                if (view != null && view.shouldSuppressFlingGesture()) {
                    return false;
                }

                if (beenSwiped) {
                    return false;
                }

                float diffX = Math.abs(e1.getX() - e2.getX());
                float diffY = Math.abs(e1.getY() - e2.getY());

                if (view != null && canScrollVertically() && getGestureAngle(diffX, diffY) > 30) {
                    return false;
                }

                if ((diffX > xpixellimit && diffY < ypixellimit) || diffX > xpixellimit * 2) {
                    beenSwiped = true;
                    if (e1.getX() > e2.getX()) {
                        onSwipe.onSwipeForward();
                    } else {
                        onSwipe.onSwipeBackward();
                    }
                    return true;
                }
            }

            return false;
        }

        private double getGestureAngle(float diffX, float diffY) {
            return Math.toDegrees(Math.atan2(diffY, diffX));
        }

        public boolean canScrollVertically() {
            NestedScrollView scrollView = view.getVerticalScrollView();

            if (scrollView != null) {
                int screenHeight = scrollView.getHeight();
                int viewHeight = scrollView.getChildAt(0).getHeight();
                return viewHeight > screenHeight;
            } else {
                return false;
            }
        }
    }

    public abstract static class View extends FrameLayout {
        public View(@NonNull Context context) {
            super(context);
        }

        public abstract boolean shouldSuppressFlingGesture();

        @Nullable
        public abstract NestedScrollView getVerticalScrollView();
    }
}
