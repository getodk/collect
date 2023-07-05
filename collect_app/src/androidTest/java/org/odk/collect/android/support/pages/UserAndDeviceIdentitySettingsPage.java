package org.odk.collect.android.support.pages;

public class UserAndDeviceIdentitySettingsPage extends Page<UserAndDeviceIdentitySettingsPage> {

    @Override
    public UserAndDeviceIdentitySettingsPage assertOnPage() {
        assertText(org.odk.collect.strings.R.string.user_and_device_identity_title);
        return this;
    }

    public FormMetadataPage clickFormMetadata() {
        clickOnString(org.odk.collect.strings.R.string.form_metadata);
        return new FormMetadataPage();
    }
}
