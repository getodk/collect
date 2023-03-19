package org.odk.collect.android.support.pages;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.doesNotExist;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import org.odk.collect.android.R;

public class ProjectSettingsPage extends Page<ProjectSettingsPage> {

    @Override
    public ProjectSettingsPage assertOnPage() {
        assertText(R.string.project_settings);
        return this;
    }

    public ProjectDisplayPage clickProjectDisplay() {
        scrollToRecyclerViewItemAndClickText(R.string.project_display_title);
        return new ProjectDisplayPage().assertOnPage();
    }

    public ProjectManagementPage clickProjectManagement() {
        scrollToRecyclerViewItemAndClickText(R.string.project_management_section_title);
        return new ProjectManagementPage().assertOnPage();
    }

    public AccessControlPage clickAccessControl() {
        scrollToRecyclerViewItemAndClickText(R.string.access_control_section_title);
        return new AccessControlPage().assertOnPage();
    }

    public UserInterfacePage clickOnUserInterface() {
        clickOnString(R.string.client);
        return new UserInterfacePage().assertOnPage();
    }

    public FormManagementPage openFormManagement() {
        clickOnString(R.string.form_management_preferences);
        return new FormManagementPage();
    }

    public ServerSettingsPage clickServerSettings() {
        clickOnString(R.string.server_settings_title);
        return new ServerSettingsPage().assertOnPage();
    }

    public MapsSettingsPage clickMaps() {
        clickOnString(R.string.maps);
        return new MapsSettingsPage().assertOnPage();
    }


    public UserAndDeviceIdentitySettingsPage clickUserAndDeviceIdentity() {
        clickOnString(R.string.user_and_device_identity_title);
        return new UserAndDeviceIdentitySettingsPage().assertOnPage();
    }

    public ProjectSettingsPage checkIfServerOptionIsDisplayed() {
        onView(withText(getTranslatedString(R.string.server_settings_title))).check(matches(isDisplayed()));
        return this;
    }

    public ProjectSettingsPage checkIfFormManagementOptionIsDisplayed() {
        onView(withText(getTranslatedString(R.string.form_management_preferences))).check(matches(isDisplayed()));
        return this;
    }

    public ProjectSettingsPage checkIfServerOptionIsNotDisplayed() {
        onView(withText("Server")).check(doesNotExist());
        return this;
    }

    public FormManagementPage clickFormManagement() {
        onView(withText(getTranslatedString(R.string.form_management_preferences))).perform(click());
        return new FormManagementPage();
    }

    public ExperimentalPage clickExperimental() {
        onView(withText(getTranslatedString(R.string.experimental))).perform(click());
        return new ExperimentalPage().assertOnPage();
    }

    public ProjectSettingsPage setAdminPassword(String password) {
        scrollToRecyclerViewItemAndClickText(R.string.set_admin_password);
        inputText(password);
        clickOKOnDialog();
        return this;
    }
}
