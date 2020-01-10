package org.odk.collect.android.support.pages;

import androidx.test.espresso.matcher.PreferenceMatchers;
import androidx.test.rule.ActivityTestRule;

import org.odk.collect.android.R;

import static androidx.test.espresso.Espresso.onData;
import static androidx.test.espresso.action.ViewActions.click;

public class ServerSettingsPage extends Page<ServerSettingsPage> {

    ServerSettingsPage(ActivityTestRule rule) {
        super(rule);
    }

    @Override
    public ServerSettingsPage assertOnPage() {
        checkIsStringDisplayed(R.string.server_preferences);
        return this;
    }

    public ServerSettingsPage clickOnServerType() {
        onData(PreferenceMatchers.withKey("protocol")).perform(click());
        return this;
    }

    public ServerSettingsPage clickAggregateUsername() {
        onData(PreferenceMatchers.withKey("username")).perform(click());
        return this;
    }

    public ServerSettingsPage clickFormListPath() {
        onData(PreferenceMatchers.withKey("formlist_url")).perform(click());
        return this;
    }

    public ServerSettingsPage clickSubmissionPath() {
        onData(PreferenceMatchers.withKey("submission_url")).perform(click());
        return this;
    }

}
