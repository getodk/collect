package org.odk.collect.android.support.pages;

import org.odk.collect.android.R;

public class FormManagementPage extends Page<FormManagementPage> {

    @Override
    public FormManagementPage assertOnPage() {
        assertToolbarTitle(getTranslatedString(R.string.form_management_preferences));
        return this;
    }

    public ListPreferenceDialog<FormManagementPage> clickUpdateForms() {
        clickOnString(R.string.form_update_mode_title);
        return new ListPreferenceDialog<>(R.string.form_update_mode_title, this).assertOnPage();
    }

    public ListPreferenceDialog<FormManagementPage> clickAutomaticUpdateFrequency() {
        clickOnString(R.string.form_update_frequency_title);
        return new ListPreferenceDialog<>(R.string.form_update_frequency_title, this).assertOnPage();
    }
}
