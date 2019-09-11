package org.odk.collect.android.espressoutils.pages;

import androidx.test.espresso.matcher.PreferenceMatchers;
import androidx.test.rule.ActivityTestRule;

import org.odk.collect.android.R;

import static androidx.test.espresso.Espresso.onData;
import static androidx.test.espresso.action.ViewActions.click;

public class GeneralSettingsPage extends Page<GeneralSettingsPage> {

    public GeneralSettingsPage(ActivityTestRule rule) {
        super(rule);
    }

    @Override
    public GeneralSettingsPage assertOnPage() {
        checkIsStringDisplayed(R.string.general_preferences);
        return this;
    }

    public GeneralSettingsPage clickOnUserInterface() {
        onData(PreferenceMatchers.withKey("user_interface")).perform(click());
        return this;
    }

    public GeneralSettingsPage clickOnLanguage() {
        onData(PreferenceMatchers.withKey("app_language")).perform(click());
        return this;
    }

    public GeneralSettingsPage clickOnSelectedLanguage(String language) {
        clickOnText(language);
        return this;
    }

    public GeneralSettingsPage clickNavigation() {
        clickOnString(R.string.navigation);
        return this;
    }

    public GeneralSettingsPage clickUseSwipesAndButtons() {
        clickOnString(R.string.swipe_buttons_navigation);
        return this;
    }
}
