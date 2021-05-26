package org.odk.collect.android.feature.formentry.backgroundlocation;

import android.Manifest;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.espresso.Espresso;
import androidx.test.rule.GrantPermissionRule;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.odk.collect.android.R;
import org.odk.collect.android.support.ActivityHelpers;
import org.odk.collect.android.support.CopyFormRule;
import org.odk.collect.android.support.FormActivityTestRule;
import org.odk.collect.android.support.AdbFormLoadingUtils;
import org.odk.collect.android.support.ResetStateRule;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

public class LocationAuditTest {
    private static final String LOCATION_AUDIT_FORM = "location-audit.xml";

    public FormActivityTestRule rule = AdbFormLoadingUtils.getFormActivityTestRuleFor(LOCATION_AUDIT_FORM);

    @Rule
    public RuleChain copyFormChain = RuleChain
            .outerRule(GrantPermissionRule.grant(Manifest.permission.ACCESS_FINE_LOCATION))
            .around(new ResetStateRule())
            .around(new CopyFormRule(LOCATION_AUDIT_FORM, true))
            .around(rule);

    @Test
    public void locationCollectionSnackbar_ShouldBeDisplayedAtFormLaunch() {
        onView(withId(com.google.android.material.R.id.snackbar_text))
                .check(matches(withText(String.format(ApplicationProvider.getApplicationContext().getString(R.string.background_location_enabled), "⋮"))));
    }

    @Test
    public void locationCollectionToggle_ShouldBeAvailable() {
        Espresso.openActionBarOverflowOrOptionsMenu(ActivityHelpers.getActivity());
        onView(withText(R.string.track_location)).check(matches(isDisplayed()));
    }
}
