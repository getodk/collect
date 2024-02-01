package org.odk.collect.android.support;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.matcher.ViewMatchers.isAssignableFrom;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static org.hamcrest.core.AllOf.allOf;

import android.app.Activity;
import android.content.Intent;
import android.view.ContextThemeWrapper;
import android.view.View;

import androidx.test.espresso.UiController;
import androidx.test.espresso.ViewAction;
import androidx.test.platform.app.InstrumentationRegistry;

import org.hamcrest.Matcher;
import org.odk.collect.android.BuildConfig;

public final class ActivityHelpers {

    private ActivityHelpers() {
    }

    public static Activity getActivity() {
        final Activity[] currentActivity = new Activity[1];
        ViewAction getActivityViewAction = new ViewAction() {
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
                    Activity activity = (Activity) view.getContext();
                    currentActivity[0] = activity;
                } else if (view.getContext() instanceof ContextThemeWrapper) {
                    Activity activity = (Activity) ((ContextThemeWrapper) view.getContext()).getBaseContext();
                    currentActivity[0] = activity;
                }
            }
        };

        onView(allOf(withId(android.R.id.content), isDisplayed())).perform(getActivityViewAction);
        return currentActivity[0];
    }

    public static Intent getLaunchIntent() {
        Intent launchIntentForPackage = InstrumentationRegistry.getInstrumentation()
                .getTargetContext()
                .getPackageManager()
                .getLaunchIntentForPackage(BuildConfig.APPLICATION_ID);

        launchIntentForPackage.addCategory(Intent.CATEGORY_LAUNCHER);
        return launchIntentForPackage;
    }
}
