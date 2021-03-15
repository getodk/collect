package org.odk.collect.android.support;

import androidx.test.espresso.intent.rule.IntentsTestRule;
import androidx.test.rule.ActivityTestRule;

import org.odk.collect.android.activities.MainMenuActivity;
import org.odk.collect.android.support.pages.MainMenuPage;

public class CollectTestRule extends ActivityTestRule<MainMenuActivity> {

    public CollectTestRule() {
        super(MainMenuActivity.class);
    }

    public MainMenuPage mainMenu() {
        return new MainMenuPage(this).assertOnPage();
    }

    public static class StubbedIntents extends IntentsTestRule<MainMenuActivity> {

        public StubbedIntents() {
            super(MainMenuActivity.class);
        }

        public MainMenuPage mainMenu() {
            return new MainMenuPage(this).assertOnPage();
        }
    }
}
