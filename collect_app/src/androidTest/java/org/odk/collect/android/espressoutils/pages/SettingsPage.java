package org.odk.collect.android.espressoutils.pages;

import androidx.test.rule.ActivityTestRule;

import org.odk.collect.android.espressoutils.Settings;

public class SettingsPage extends Page<SettingsPage> {

    SettingsPage(ActivityTestRule rule) {
        super(rule);
    }

    public SettingsPage clickOnUserInterface() {
        Settings.clickOnUserInterface();
        return this;
    }

    public SettingsPage clickOnLanguage() {
        Settings.clickOnLanguage();
        return this;
    }

    public SettingsPage clickOnSelectedLanguage(String language) {
        Settings.clickOnSelectedLanguage(language);
        return this;
    }

    public SettingsPage clickNavigation() {
        Settings.clickNavigation();
        return this;
    }

    public SettingsPage clickUseSwipesAndButtons() {
        Settings.clickUseSwipesAndButtons();
        return this;
    }
}
