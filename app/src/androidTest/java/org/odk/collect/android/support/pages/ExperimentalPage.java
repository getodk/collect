package org.odk.collect.android.support.pages;

import org.odk.collect.android.R;

public class ExperimentalPage extends Page<ExperimentalPage> {

    @Override
    public ExperimentalPage assertOnPage() {
        assertToolbarTitle(getTranslatedString(R.string.experimental));
        return this;
    }
}
