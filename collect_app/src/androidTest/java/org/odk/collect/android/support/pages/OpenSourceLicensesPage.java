package org.odk.collect.android.support.pages;

import androidx.test.rule.ActivityTestRule;

class OpenSourceLicensesPage extends Page<OpenSourceLicensesPage> {

    OpenSourceLicensesPage(ActivityTestRule rule) {
        super(rule);
    }

    @Override
    public OpenSourceLicensesPage assertOnPage() {
        waitForText("Open Source Licenses");
        checkIfWebViewActivityIsDisplayed();
        return this;
    }
}
