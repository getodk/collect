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
}
