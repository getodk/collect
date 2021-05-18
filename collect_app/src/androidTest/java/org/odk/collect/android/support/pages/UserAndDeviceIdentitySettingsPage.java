package org.odk.collect.android.support.pages;

import org.odk.collect.android.R;

public class UserAndDeviceIdentitySettingsPage extends Page<UserAndDeviceIdentitySettingsPage> {

    @Override
    public UserAndDeviceIdentitySettingsPage assertOnPage() {
        assertText(R.string.user_and_device_identity_title);
        return this;
    }

    public FormMetadataPage clickFormMetadata() {
        clickOnString(R.string.form_metadata);
        return new FormMetadataPage();
    }
}
