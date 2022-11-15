package org.odk.collect.android.support.actions;

import static androidx.test.espresso.matcher.ViewMatchers.isRoot;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.view.ContextThemeWrapper;
import android.view.View;
import android.view.ViewGroup;

import androidx.test.espresso.UiController;
import androidx.test.espresso.ViewAction;

import org.hamcrest.Matcher;

public class RotateAction implements ViewAction {

    private final int screenOrientation;

    public RotateAction(int screenOrientation) {
        this.screenOrientation = screenOrientation;
    }

    @Override
    public Matcher<View> getConstraints() {
        return isRoot();
    }

    @Override
    public String getDescription() {
        return "";
    }

    @Override
    public void perform(UiController uiController, View view) {
        uiController.loopMainThreadUntilIdle();

        Activity activity = getCurrentActivity(view);

        if (activity != null) {
            activity.setRequestedOrientation(screenOrientation);
        } else {
            throw new IllegalStateException("We don't know how to get the current Activity in this scenario");
        }
    }

    private Activity getCurrentActivity(View view) {
        Activity activity = getActivityFromContext(view.getContext());

        if (activity != null) {
            return activity;
        } else if (view instanceof ViewGroup) {
            ViewGroup viewGroup = (ViewGroup) view;

            for (int i = 0; i < viewGroup.getChildCount(); i++) {
                Activity childViewActivity = getCurrentActivity(viewGroup.getChildAt(0));
                if (childViewActivity != null) {
                    return childViewActivity;
                }
            }
        }

        return null;
    }

    private Activity getActivityFromContext(Context context) {
        if (context instanceof Activity) {
            return (Activity) context;
        } else if (context instanceof Application) {
            return null;
        } else if (context instanceof ContextThemeWrapper) {
            ContextThemeWrapper wrapper = (ContextThemeWrapper) context;
            return getActivityFromContext(wrapper.getBaseContext());
        }

        return null;
    }
}
