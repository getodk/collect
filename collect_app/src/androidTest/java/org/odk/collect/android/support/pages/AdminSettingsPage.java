package org.odk.collect.android.support.pages;

import org.odk.collect.android.R;

public class AdminSettingsPage extends Page<AdminSettingsPage> {

    @Override
    public AdminSettingsPage assertOnPage() {
        assertText(R.string.admin_preferences);
        return this;
    }

    public GeneralSettingsPage clickGeneralSettings() {
        scrollToRecyclerViewItemAndClickText(getTranslatedString(R.string.project_settings));
        return new GeneralSettingsPage().assertOnPage();
    }
}
