package org.odk.collect.android.support;

import androidx.test.core.app.ActivityScenario;

import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;
import org.odk.collect.android.R;
import org.odk.collect.android.activities.SplashScreenActivity;
import org.odk.collect.android.support.pages.FirstLaunchPage;
import org.odk.collect.android.support.pages.MainMenuPage;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

public class CollectTestRule implements TestRule {

    private final boolean skipLaunchScreen;

    public CollectTestRule() {
        this(true);
    }

    public CollectTestRule(boolean skipLaunchScreen) {
        this.skipLaunchScreen = skipLaunchScreen;
    }

    @Override
    public Statement apply(Statement base, Description description) {
        return new Statement() {
            @Override
            public void evaluate() throws Throwable {
                ActivityScenario.launch(SplashScreenActivity.class);

                if (skipLaunchScreen) {
                    onView(withText(R.string.configure_later)).perform(click());
                }

                base.evaluate();
            }
        };
    }

    public MainMenuPage startAtMainMenu() {
        return new MainMenuPage().assertOnPage();
    }

    public FirstLaunchPage startAtFirstLaunch() {
        return new FirstLaunchPage().assertOnPage();
    }
}
