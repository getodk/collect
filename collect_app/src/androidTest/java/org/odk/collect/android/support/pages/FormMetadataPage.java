package org.odk.collect.android.support.pages;

public class FormMetadataPage extends PreferencePage<FormMetadataPage> {

    @Override
    public FormMetadataPage assertOnPage() {
        assertText(org.odk.collect.strings.R.string.form_metadata_title);
        return this;
    }

    public FormMetadataPage clickEmail() {
        clickOnString(org.odk.collect.strings.R.string.email);
        return this;
    }

    public FormMetadataPage clickUsername() {
        clickOnString(org.odk.collect.strings.R.string.username);
        return this;
    }

    public FormMetadataPage clickPhoneNumber() {
        clickOnString(org.odk.collect.strings.R.string.phone_number);
        return this;
    }
}
