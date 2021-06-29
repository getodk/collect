package org.odk.collect.android.support;

import android.content.Intent;

import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;

import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;
import org.odk.collect.android.R;
import org.odk.collect.android.activities.AndroidShortcutsActivity;
import org.odk.collect.android.activities.SplashScreenActivity;
import org.odk.collect.android.support.pages.FirstLaunchPage;
import org.odk.collect.android.support.pages.MainMenuPage;
import org.odk.collect.android.support.pages.Page;
import org.odk.collect.android.support.pages.ShortcutsPage;

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

                if (!CopyFormRule.projectCreated && skipLaunchScreen) {
                    onView(withText(R.string.try_demo)).perform(click());
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

    public ShortcutsPage launchShortcuts() {
        ActivityScenario<AndroidShortcutsActivity> scenario = ActivityScenario.launch(AndroidShortcutsActivity.class);
        return new ShortcutsPage(scenario).assertOnPage();
    }

    public <T extends Page<T>> T launch(Intent intent, T destination) {
        /*
        This can't use ActivityScenario.launch because of: https://github.com/android/android-test/issues/496
         */
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        ApplicationProvider.getApplicationContext().startActivity(intent);
        return destination.assertOnPage();
    }
}
