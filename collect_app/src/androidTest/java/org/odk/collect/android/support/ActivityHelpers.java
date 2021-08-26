package org.odk.collect.android.support;

import android.app.Activity;
import android.view.View;

import androidx.test.espresso.Espresso;
import androidx.test.espresso.UiController;
import androidx.test.espresso.ViewAction;
import androidx.test.espresso.matcher.ViewMatchers;

import org.hamcrest.Matcher;
import org.hamcrest.core.AllOf;

import static androidx.test.espresso.matcher.ViewMatchers.isAssignableFrom;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;

public final class ActivityHelpers {

    private ActivityHelpers() {
    }

    public static Activity getActivity() {
        final Activity[] currentActivity = new Activity[1];
        Espresso.onView(AllOf.allOf(ViewMatchers.withId(android.R.id.content), isDisplayed())).perform(new ViewAction() {
            @Override
            public Matcher<View> getConstraints() {
                return isAssignableFrom(View.class);
            }

            @Override
            public String getDescription() {
                return "getting text from a TextView";
            }

            @Override
            public void perform(UiController uiController, View view) {
                if (view.getContext() instanceof Activity) {
                    Activity activity1 = (Activity) view.getContext();
                    currentActivity[0] = activity1;
                }
            }
        });
        return currentActivity[0];
    }
}
