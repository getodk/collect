package org.odk.collect.android.support.pages;

import org.odk.collect.android.R;

public class MapsSettingsPage extends Page<MapsSettingsPage> {

    @Override
    public MapsSettingsPage assertOnPage() {
        assertText(R.string.maps);
        return this;
    }
}
