package org.odk.collect.android.support.pages;

import android.content.Intent;

import androidx.test.core.app.ActivityScenario;

import org.odk.collect.android.R;
import org.odk.collect.android.external.AndroidShortcutsActivity;

public class ShortcutsPage extends Page<ShortcutsPage> {

    private final ActivityScenario<AndroidShortcutsActivity> scenario;

    public ShortcutsPage(ActivityScenario<AndroidShortcutsActivity> scenario) {
        this.scenario = scenario;
    }

    @Override
    public ShortcutsPage assertOnPage() {
        assertText(R.string.select_odk_shortcut);
        return this;
    }

    public Intent selectForm(String formName) {
        clickOnText(formName);
        return scenario.getResult().getResultData();
    }
}
