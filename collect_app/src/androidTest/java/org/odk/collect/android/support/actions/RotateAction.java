package org.odk.collect.android.support.actions;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.view.ContextThemeWrapper;
import android.view.View;

import androidx.test.espresso.UiController;
import androidx.test.espresso.ViewAction;

import org.hamcrest.Matcher;

import static androidx.test.espresso.matcher.ViewMatchers.isRoot;

public class RotateAction implements ViewAction {

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

        ContextThemeWrapper context = (ContextThemeWrapper) view.getContext();
        Activity activity = (Activity) context.getBaseContext();
        activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
    }
}
