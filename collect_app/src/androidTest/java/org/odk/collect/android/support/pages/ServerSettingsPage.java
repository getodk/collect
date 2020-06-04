package org.odk.collect.android.support.pages;

import androidx.test.rule.ActivityTestRule;

import org.odk.collect.android.R;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

public class ServerSettingsPage extends Page<ServerSettingsPage> {

    public ServerSettingsPage(ActivityTestRule rule) {
        super(rule);
    }

    @Override
    public ServerSettingsPage assertOnPage() {
        checkIsStringDisplayed(R.string.server_preferences);
        return this;
    }

    public ServerSettingsPage clickOnServerType() {
        onView(withText(getTranslatedString(R.string.type))).perform(click());
        return this;
    }

    public ServerSettingsPage clickAggregateUsername() {
        onView(withText(getTranslatedString(R.string.username))).perform(click());
        return this;
    }

    public ServerSettingsPage clickOnURL() {
        onView(withText(getTranslatedString(R.string.server_url))).perform(click());
        return this;
    }

    public CustomServerPathsPage clickCustomServerPaths() {
        onView(withText(getTranslatedString(R.string.custom_server_paths))).perform(click());
        return new CustomServerPathsPage(rule).assertOnPage();
    }
}
