package org.odk.collect.android.support.pages;

public class ServerSettingsPage extends Page<ServerSettingsPage> {

    @Override
    public ServerSettingsPage assertOnPage() {
        assertText(org.odk.collect.strings.R.string.server_preferences);
        return this;
    }

    public ServerSettingsPage clickServerUsername() {
        clickOnString(org.odk.collect.strings.R.string.username);
        return this;
    }

    public ServerSettingsPage clickOnURL() {
        clickOnString(org.odk.collect.strings.R.string.server_url);
        return this;
    }

    public ServerSettingsPage clickServerPassword() {
        clickOnString(org.odk.collect.strings.R.string.password);
        return this;
    }
}
