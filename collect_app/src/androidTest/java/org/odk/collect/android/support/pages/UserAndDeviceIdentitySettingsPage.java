package org.odk.collect.android.support.pages;

import androidx.test.espresso.matcher.PreferenceMatchers;
import androidx.test.rule.ActivityTestRule;

import org.odk.collect.android.R;

import static androidx.test.espresso.Espresso.onData;
import static androidx.test.espresso.action.ViewActions.click;

public class UserAndDeviceIdentitySettingsPage extends Page<UserAndDeviceIdentitySettingsPage> {

    public UserAndDeviceIdentitySettingsPage(ActivityTestRule rule) {
        super(rule);
    }

    @Override
    public UserAndDeviceIdentitySettingsPage assertOnPage() {
        checkIsStringDisplayed(R.string.user_and_device_identity_title);
        return this;
    }

    public UserAndDeviceIdentitySettingsPage clickFormMetadata() {
        onData(PreferenceMatchers.withKey("form_metadata")).perform(click());
        return this;
    }

    public UserAndDeviceIdentitySettingsPage clickMetadataEmail() {
        onData(PreferenceMatchers.withKey("metadata_email")).perform(click());
        return this;
    }

    public UserAndDeviceIdentitySettingsPage clickMetadataUsername() {
        onData(PreferenceMatchers.withKey("metadata_username")).perform(click());
        return this;
    }
}
