package org.odk.collect.android.support;

import androidx.test.espresso.intent.rule.IntentsTestRule;
import androidx.test.rule.ActivityTestRule;

import org.odk.collect.android.R;
import org.odk.collect.android.activities.MainMenuActivity;
import org.odk.collect.android.activities.SplashScreenActivity;
import org.odk.collect.android.support.pages.MainMenuPage;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

public class CollectTestRule extends ActivityTestRule<SplashScreenActivity> {

    private final boolean skipProject;

    public CollectTestRule() {
        this(true);
    }

    public CollectTestRule(boolean skipProject) {
        super(SplashScreenActivity.class);
        this.skipProject = skipProject;
    }

    @Override
    protected void afterActivityLaunched() {
        super.afterActivityLaunched();

        if (skipProject) {
            onView(withText(R.string.configure_later)).perform(click());
        }
    }

    public MainMenuPage mainMenu() {
        return new MainMenuPage().assertOnPage();
    }

    public static class StubbedIntents extends IntentsTestRule<MainMenuActivity> {

        public StubbedIntents() {
            super(MainMenuActivity.class);
        }

        public MainMenuPage mainMenu() {
            return new MainMenuPage().assertOnPage();
        }
    }
}
