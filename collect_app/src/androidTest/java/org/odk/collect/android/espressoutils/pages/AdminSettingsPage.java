package org.odk.collect.android.espressoutils.pages;

import androidx.test.rule.ActivityTestRule;

import org.odk.collect.android.R;

public class AdminSettingsPage extends Page<AdminSettingsPage> {

    AdminSettingsPage(ActivityTestRule rule) {
        super(rule);
    }

    @Override
    public AdminSettingsPage assertOnPage() {
        checkIsStringDisplayed(R.string.admin_preferences);
        return this;
    }
}
