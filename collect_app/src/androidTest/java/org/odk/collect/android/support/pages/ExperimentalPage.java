package org.odk.collect.android.support.pages;

public class ExperimentalPage extends Page<ExperimentalPage> {

    @Override
    public ExperimentalPage assertOnPage() {
        assertToolbarTitle(getTranslatedString(org.odk.collect.strings.R.string.experimental));
        return this;
    }
}
