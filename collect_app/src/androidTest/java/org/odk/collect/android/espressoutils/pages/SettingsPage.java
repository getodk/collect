package org.odk.collect.android.espressoutils.pages;

import androidx.test.espresso.matcher.PreferenceMatchers;
import androidx.test.rule.ActivityTestRule;

import org.odk.collect.android.R;

import static androidx.test.espresso.Espresso.onData;
import static androidx.test.espresso.action.ViewActions.click;

public class SettingsPage extends Page<SettingsPage> {

    SettingsPage(ActivityTestRule rule) {
        super(rule);
    }

    public SettingsPage clickOnUserInterface() {
        onData(PreferenceMatchers.withKey("user_interface")).perform(click());
        return this;
    }

    public SettingsPage clickOnLanguage() {
        onData(PreferenceMatchers.withKey("app_language")).perform(click());
        return this;
    }

    public SettingsPage clickOnSelectedLanguage(String language) {
        clickOnText(language);
        return this;
    }

    public SettingsPage clickNavigation() {
        clickOnString(R.string.navigation);
        return this;
    }

}
