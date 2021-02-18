package org.odk.collect.android.support.pages;

import androidx.test.espresso.contrib.RecyclerViewActions;
import androidx.test.rule.ActivityTestRule;

import org.odk.collect.android.R;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.scrollTo;
import static androidx.test.espresso.assertion.ViewAssertions.doesNotExist;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.hasDescendant;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.isEnabled;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
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
        clickOnString(R.string.client);
        return new UserInterfacePage(rule).assertOnPage();
    }

    public GeneralSettingsPage openFormManagement() {
        clickOnString(R.string.form_management_preferences);
        return this;
    }

    public GeneralSettingsPage openConstraintProcessing() {
        scrollToRecyclerViewItemAndClickText(getTranslatedString(R.string.constraint_behavior_title));
        return this;
    }

    public GeneralSettingsPage openShowGuidanceForQuestions() {
        scrollToRecyclerViewItemAndClickText(getTranslatedString(R.string.guidance_hint_title));
        return this;
    }

    public ServerSettingsPage clickServerSettings() {
        clickOnString(R.string.server_settings_title);
        return new ServerSettingsPage(rule).assertOnPage();
    }

    public MapsSettingsPage clickMaps() {
        clickOnString(R.string.maps);
        return new MapsSettingsPage(rule).assertOnPage();
    }


    public UserAndDeviceIdentitySettingsPage clickUserAndDeviceIdentity() {
        clickOnString(R.string.user_and_device_identity_title);
        return new UserAndDeviceIdentitySettingsPage(rule).assertOnPage();
    }

    public GeneralSettingsPage checkIfServerOptionIsDisplayed() {
        onView(withText(getTranslatedString(R.string.server_settings_title))).check(matches(isDisplayed()));
        return this;
    }

    public GeneralSettingsPage checkIfUserInterfaceOptionIsDisplayed() {
        onView(withText(getTranslatedString(R.string.client))).check(matches(isDisplayed()));
        return this;
    }

    public GeneralSettingsPage checkIfMapsOptionIsDisplayed() {
        onView(withText(getTranslatedString(R.string.maps))).check(matches(isDisplayed()));
        return this;
    }

    public GeneralSettingsPage checkIfFormManagementOptionIsDisplayed() {
        onView(withText(getTranslatedString(R.string.form_management_preferences))).check(matches(isDisplayed()));
        return this;
    }

    public GeneralSettingsPage checkIfUserAndDeviceIdentityIsDisplayed() {
        onView(withText(getTranslatedString(R.string.user_and_device_identity_title))).check(matches(isDisplayed()));
        return this;
    }

    public GeneralSettingsPage scrollToConstraintProcessing() {
        onView(withId(R.id.recycler_view)).perform(RecyclerViewActions
                .actionOnItem(hasDescendant(withText(getTranslatedString(R.string.constraint_behavior_title))), scrollTo()));
        return this;
    }

    public GeneralSettingsPage checkIfConstraintProcessingIsDisabled() {
        onView(withText(getTranslatedString(R.string.constraint_behavior_title))).check(matches(not(isEnabled())));
        return this;
    }

    public GeneralSettingsPage clickOnAutoSend() {
        clickOnString(R.string.autosend_selector_title);
        return this;
    }

    public GeneralSettingsPage clickOnDefaultToFinalized() {
        scrollToRecyclerViewItemAndClickText(getTranslatedString(R.string.default_completed));
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
