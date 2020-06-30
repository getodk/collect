package org.odk.collect.android.support.pages;

import androidx.test.espresso.action.ViewActions;
import androidx.test.espresso.base.Default;
import androidx.test.espresso.matcher.PreferenceMatchers;
import androidx.test.rule.ActivityTestRule;

import org.odk.collect.android.R;

import static androidx.test.espresso.Espresso.onData;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.scrollTo;
import static androidx.test.espresso.assertion.ViewAssertions.doesNotExist;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.isEnabled;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.CoreMatchers.not;
import static org.odk.collect.android.support.actions.NestedScrollToAction.nestedScrollTo;

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
        clickOnString(R.string.client);
        return new UserInterfacePage(rule).assertOnPage();
    }

    public GeneralSettingsPage openFormManagement() {
        clickOnString(R.string.form_management_preferences);
        return this;
    }

    public GeneralSettingsPage openConstraintProcessing() {
        scrollToAndClickText(getTranslatedString(R.string.constraint_behavior_title));
//        onView(withText(getTranslatedString(R.string.constraint_behavior_title))).perform(nestedScrollTo(), click());
//        onData(PreferenceMatchers.withKey("constraint_behavior")).perform(ViewActions.scrollTo());
//        onData(PreferenceMatchers.withKey("constraint_behavior")).perform(click());
        return this;
    }

    public GeneralSettingsPage openShowGuidanceForQuestions() {
        onView(withText(getTranslatedString(R.string.guidance_hint_title))).perform(nestedScrollTo(), click());
//        clickOnString(R.string.guidance_hint_title);
//        onData(PreferenceMatchers.withKey("guidance_hint")).perform(ViewActions.scrollTo());
//        onData(PreferenceMatchers.withKey("guidance_hint")).perform(click());
        return this;
    }

    public ServerSettingsPage openServerSettings() {
        clickOnString(R.string.server);
        return new ServerSettingsPage(rule).assertOnPage();
    }

    public MapsSettingsPage clickMaps() {
        onData(PreferenceMatchers.withKey("maps")).perform(click());
        return new MapsSettingsPage(rule).assertOnPage();
    }


    public UserAndDeviceIdentitySettingsPage clickUserAndDeviceIdentity() {
        clickOnString(R.string.user_and_device_identity_title);
        return new UserAndDeviceIdentitySettingsPage(rule).assertOnPage();
    }

    public GeneralSettingsPage checkIfServerOptionIsDisplayed() {
        onView(withText(getTranslatedString(R.string.server))).check(matches(isDisplayed()));
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
        clickOnString(R.string.autosend_selector_title);
        return this;
    }

    public GeneralSettingsPage clickOnDefaultToFinalized() {
        onView(withText(getTranslatedString(R.string.default_completed))).perform(nestedScrollTo(), click());
//        onData(PreferenceMatchers.withKey("default_completed")).perform(ViewActions.scrollTo());
//        onData(PreferenceMatchers.withKey("default_completed")).perform(click());
        return this;
    }

    public GeneralSettingsPage checkIfServerOptionIsNotDisplayed() {
        onView(withText("Server")).check(doesNotExist());
        return this;
    }

    public FormManagementPage clickFormManagement() {
        onView(withText(getTranslatedString(R.string.form_management_preferences))).perform(click());
        return new FormManagementPage(rule);
    }

    public ExperimentalPage clickExperimental() {
        onView(withText(getTranslatedString(R.string.experimental))).perform(click());
        return new ExperimentalPage(rule).assertOnPage();
    }
}
