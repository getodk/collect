package org.odk.collect.android.support;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.Intent;

import androidx.test.core.app.ActivityScenario;

import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;
import org.odk.collect.android.R;
import org.odk.collect.android.activities.SplashScreenActivity;
import org.odk.collect.android.external.AndroidShortcutsActivity;
import org.odk.collect.android.support.pages.FirstLaunchPage;
import org.odk.collect.android.support.pages.MainMenuPage;
import org.odk.collect.android.support.pages.Page;
import org.odk.collect.android.support.pages.ShortcutsPage;

import java.util.function.Consumer;

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
                restart();

                if (!CopyFormRule.projectCreated && skipLaunchScreen) {
                    onView(withText(R.string.try_demo)).perform(click());
                }

                base.evaluate();
            }
        };
    }

    public CollectTestRule restart() {
        ActivityScenario.launch(SplashScreenActivity.class);
        return this;
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
        ActivityScenario.launch(intent);
        return destination.assertOnPage();
    }

    public <T extends Page<T>> Instrumentation.ActivityResult launchForResult(Intent intent, T destination, Consumer<T> actions) {
        ActivityScenario<Activity> scenario = ActivityScenario.launch(intent);
        destination.assertOnPage();
        actions.accept(destination);
        return scenario.getResult();
    }
}
