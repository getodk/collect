package org.odk.collect.android.support.pages;

import androidx.test.rule.ActivityTestRule;

import org.odk.collect.android.R;

public class UpdateBlankFormsModePage extends Page<UpdateBlankFormsModePage> {

    UpdateBlankFormsModePage(ActivityTestRule rule) {
        super(rule);
    }

    @Override
    public UpdateBlankFormsModePage assertOnPage() {
        assertText(R.string.form_update_mode_settings_title);
        return this;
    }
}
