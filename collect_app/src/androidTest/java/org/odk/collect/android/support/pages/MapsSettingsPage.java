package org.odk.collect.android.support.pages;

public class MapsSettingsPage extends Page<MapsSettingsPage> {

    @Override
    public MapsSettingsPage assertOnPage() {
        assertText(org.odk.collect.strings.R.string.maps);
        return this;
    }
}
