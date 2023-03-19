package org.odk.collect.android.support.pages;

import org.odk.collect.android.R;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

public class ServerSettingsPage extends Page<ServerSettingsPage> {

    @Override
    public ServerSettingsPage assertOnPage() {
        assertText(R.string.server_preferences);
        return this;
    }

    public ServerSettingsPage clickOnServerType() {
        onView(withText(getTranslatedString(R.string.type))).perform(click());
        return this;
    }

    public ServerSettingsPage clickServerUsername() {
        onView(withText(getTranslatedString(R.string.username))).perform(click());
        return this;
    }

    public ServerSettingsPage clickOnURL() {
        onView(withText(getTranslatedString(R.string.server_url))).perform(click());
        return this;
    }

    public ServerSettingsPage clickServerPassword() {
        onView(withText(getTranslatedString(R.string.password))).perform(click());
        return this;
    }
}
