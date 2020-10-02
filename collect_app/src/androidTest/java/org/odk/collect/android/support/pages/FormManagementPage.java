package org.odk.collect.android.support.pages;

import androidx.test.rule.ActivityTestRule;

import org.odk.collect.android.R;

public class FormManagementPage extends Page<FormManagementPage> {

    public FormManagementPage(ActivityTestRule rule) {
        super(rule);
    }

    @Override
    public FormManagementPage assertOnPage() {
        assertToolbarTitle(getTranslatedString(R.string.form_management_preferences));
        return this;
    }

    public ListPreferenceDialog<FormManagementPage> clickUpdateForms() {
        clickOnString(R.string.form_update_mode_title);
        return new ListPreferenceDialog<>(R.string.form_update_mode_title, this, rule).assertOnPage();
    }

    public ListPreferenceDialog<FormManagementPage> clickAutomaticUpdateFrequency() {
        clickOnString(R.string.form_update_frequency_title);
        return new ListPreferenceDialog<>(R.string.form_update_frequency_title, this, rule).assertOnPage();
    }
}
