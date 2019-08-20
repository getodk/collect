package org.odk.collect.android.formentry;

import android.Manifest;

import androidx.test.espresso.assertion.ViewAssertions;
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
import org.odk.collect.android.support.matchers.RecyclerViewMatcher;
import org.odk.collect.android.test.FormLoadingUtils;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.odk.collect.android.support.matchers.RecyclerViewMatcher.withRecyclerView;

public class FormHierarchyTest2 {

    @Rule
    public IntentsTestRule<FormEntryActivity> activityTestRule = FormLoadingUtils.getFormActivityTestRuleFor("repeatCount.xml");

    @Rule
    public RuleChain copyFormChain = RuleChain
            .outerRule(GrantPermissionRule.grant(
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)
            )
            .around(new ResetStateRule())
            .around(new CopyFormRule("repeatCount.xml", null));

    @Test
    public void repeatCountTest() {
        FormEntry.putText("2");
        FormEntry.clickGoToIconInForm();
        onView(withId(R.id.list)).check(ViewAssertions.matches(RecyclerViewMatcher.withListSize(3)));
        onView(withRecyclerView(R.id.list)
                .atPositionOnView(0, R.id.primary_text))
                .check(matches(withText("How many guests are in your party?")));
        onView(withRecyclerView(R.id.list)
                .atPositionOnView(0, R.id.secondary_text))
                .check(matches(withText("2")));
        onView(withRecyclerView(R.id.list)
                .atPositionOnView(1, R.id.primary_text))
                .check(matches(withText("Please provide details for each guest.")));
        onView(withRecyclerView(R.id.list)
                .atPositionOnView(2, R.id.primary_text))
                .check(matches(withText("Guest details")));
        onView(withRecyclerView(R.id.list)
                .atPositionOnView(2, R.id.secondary_text))
                .check(matches(withText("Repeatable Group")));
        FormEntry.clickOnText("Guest details");
        onView(withId(R.id.list)).check(ViewAssertions.matches(RecyclerViewMatcher.withListSize(2)));
        onView(withRecyclerView(R.id.list)
                .atPositionOnView(0, R.id.primary_text))
                .check(matches(withText("Guest details > 1")));
        onView(withRecyclerView(R.id.list)
                .atPositionOnView(1, R.id.primary_text))
                .check(matches(withText("Guest details > 2")));
        FormEntry.clickJumpStartButton();
        FormEntry.putText("1");
        FormEntry.clickGoToIconInForm();
        FormEntry.clickOnText("Guest details");
        onView(withId(R.id.list)).check(ViewAssertions.matches(RecyclerViewMatcher.withListSize(1)));
        onView(withRecyclerView(R.id.list)
                .atPositionOnView(0, R.id.primary_text))
                .check(matches(withText("Guest details > 1")));
    }
}
