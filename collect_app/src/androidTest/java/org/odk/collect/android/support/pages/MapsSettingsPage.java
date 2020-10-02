package org.odk.collect.android.support.pages;

import androidx.test.rule.ActivityTestRule;

import org.odk.collect.android.R;

public class MapsSettingsPage extends Page<MapsSettingsPage> {

    MapsSettingsPage(ActivityTestRule rule) {
        super(rule);
    }

    @Override
    public MapsSettingsPage assertOnPage() {
        assertText(R.string.maps);
        return this;
    }
}
