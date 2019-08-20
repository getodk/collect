package org.odk.collect.android.formentry;

import android.Manifest;

import androidx.test.espresso.intent.rule.IntentsTestRule;
import androidx.test.rule.GrantPermissionRule;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.odk.collect.android.R;
import org.odk.collect.android.activities.FormEntryActivity;
import org.odk.collect.android.espressoutils.FormEntry;
import org.odk.collect.android.support.CopyFormRule;
import org.odk.collect.android.support.ResetStateRule;
import org.odk.collect.android.test.FormLoadingUtils;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.odk.collect.android.support.matchers.RecyclerViewMatcher.withRecyclerView;

public class FormHierarchyTest1 {

    @Rule
    public IntentsTestRule<FormEntryActivity> activityTestRule = FormLoadingUtils.getFormActivityTestRuleFor("formHierarchy1.xml");

    @Rule
    public RuleChain copyFormChain = RuleChain
            .outerRule(GrantPermissionRule.grant(
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)
            )
            .around(new ResetStateRule())
            .around(new CopyFormRule("formHierarchy1.xml", null));

    @Test
    //https://github.com/opendatakit/collect/issues/2871
    public void test() {
        FormEntry.clickGoToIconInForm();

        onView(withRecyclerView(R.id.list)
                .atPositionOnView(0, R.id.primary_text))
                .check(matches(withText("what is your name?")));

        onView(withRecyclerView(R.id.list)
                .atPositionOnView(1, R.id.primary_text))
                .check(matches(withText("what is your age?")));
    }
}
