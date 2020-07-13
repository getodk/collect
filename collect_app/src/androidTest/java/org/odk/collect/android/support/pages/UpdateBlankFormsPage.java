package org.odk.collect.android.support.pages;

import androidx.test.rule.ActivityTestRule;

import org.odk.collect.android.R;

public class UpdateBlankFormsPage extends Page<UpdateBlankFormsPage> {

    UpdateBlankFormsPage(ActivityTestRule rule) {
        super(rule);
    }

    @Override
    public UpdateBlankFormsPage assertOnPage() {
        assertToolbarTitle(R.string.update_blank_forms);
        return this;
    }
}
