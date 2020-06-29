package org.odk.collect.android.support.pages;

import androidx.test.espresso.action.ViewActions;
import androidx.test.espresso.matcher.PreferenceMatchers;
import androidx.test.rule.ActivityTestRule;

import org.odk.collect.android.R;

import static androidx.test.espresso.Espresso.onData;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.doesNotExist;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.isEnabled;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.CoreMatchers.not;

public class GeneralSettingsPage extends Page<GeneralSettingsPage> {

    public GeneralSettingsPage(ActivityTestRule rule) {
        super(rule);
    }

    @Override
    public GeneralSettingsPage assertOnPage() {
        assertText(R.string.general_preferences);
        return this;
    }

    public UserInterfacePage clickOnUserInterface() {
        onData(PreferenceMatchers.withKey("user_interface")).perform(click());
        return new UserInterfacePage(rule).assertOnPage();
    }

    public GeneralSettingsPage openFormManagement() {
        onData(PreferenceMatchers.withKey("form_management")).perform(click());
        return this;
    }

    public GeneralSettingsPage openConstraintProcessing() {
        onData(PreferenceMatchers.withKey("constraint_behavior")).perform(ViewActions.scrollTo());
        onData(PreferenceMatchers.withKey("constraint_behavior")).perform(click());
        return this;
    }

    public GeneralSettingsPage openShowGuidanceForQuestions() {
        onData(PreferenceMatchers.withKey("guidance_hint")).perform(ViewActions.scrollTo());
        onData(PreferenceMatchers.withKey("guidance_hint")).perform(click());
        return this;
    }

    public ServerSettingsPage clickServerSettings() {
        onData(PreferenceMatchers.withKey("protocol")).perform(click());
        return new ServerSettingsPage(rule).assertOnPage();
    }

    public UserAndDeviceIdentitySettingsPage clickUserAndDeviceIdentity() {
        onData(PreferenceMatchers.withKey("user_and_device_identity")).perform(click());
        return new UserAndDeviceIdentitySettingsPage(rule).assertOnPage();
    }

    public GeneralSettingsPage checkIfServerOptionIsDisplayed() {
        onData(PreferenceMatchers.withKey("protocol")).check(matches(isDisplayed()));
        return this;
    }

    public GeneralSettingsPage checkIfUserInterfaceOptionIsDisplayed() {
        onData(PreferenceMatchers.withKey("user_interface")).check(matches(isDisplayed()));
        return this;
    }

    public GeneralSettingsPage checkIfMapsOptionIsDisplayed() {
        onData(PreferenceMatchers.withKey("maps")).check(matches(isDisplayed()));
        return this;
    }

    public GeneralSettingsPage checkIfFormManagementOptionIsDisplayed() {
        onData(PreferenceMatchers.withKey("form_management")).check(matches(isDisplayed()));
        return this;
    }

    public GeneralSettingsPage checkIfUserAndDeviceIdentityIsDisplayed() {
        onData(PreferenceMatchers.withKey("user_and_device_identity")).check(matches(isDisplayed()));
        return this;
    }

    public GeneralSettingsPage scrollToConstraintProcessing() {
        onData(PreferenceMatchers.withKey("constraint_behavior")).perform(ViewActions.scrollTo());
        return this;
    }

    public GeneralSettingsPage checkIfConstraintProcessingIsDisabled() {
        onData(PreferenceMatchers.withKey("constraint_behavior")).check(matches(not(isEnabled())));
        return this;
    }

    public GeneralSettingsPage clickOnAutoSend() {
        onData(PreferenceMatchers.withKey("autosend")).perform(click());
        return this;
    }

    public GeneralSettingsPage clickOnDefaultToFinalized() {
        onData(PreferenceMatchers.withKey("default_completed")).perform(ViewActions.scrollTo());
        onData(PreferenceMatchers.withKey("default_completed")).perform(click());
        return this;
    }

    public GeneralSettingsPage checkIfServerOptionIsNotDisplayed() {
        onView(withText("Server")).check(doesNotExist());
        return this;
    }
}
