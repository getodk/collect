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
import static androidx.test.espresso.matcher.ViewMatchers.withId;

public class FormHierarchyTest3 {

    @Rule
    public IntentsTestRule<FormEntryActivity> activityTestRule = FormLoadingUtils.getFormActivityTestRuleFor("formHierarchy3.xml");

    @Rule
    public RuleChain copyFormChain = RuleChain
            .outerRule(GrantPermissionRule.grant(
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)
            )
            .around(new ResetStateRule())
            .around(new CopyFormRule("formHierarchy3.xml", null));

    @Test
    //https://github.com/opendatakit/collect/issues/2936
    public void test1() {
        FormEntry.swipeToNextQuestion();
        FormEntry.swipeToNextQuestion();
        FormEntry.swipeToNextQuestion();
        FormEntry.swipeToNextQuestion();
        FormEntry.swipeToNextQuestion();
        FormEntry.swipeToNextQuestion();
        FormEntry.clickOnString(R.string.add_another);
        FormEntry.swipeToNextQuestion();
        FormEntry.clickOnString(R.string.add_another);
        FormEntry.swipeToNextQuestion();
        FormEntry.clickOnString(R.string.add_repeat_no);
        FormEntry.clickOnString(R.string.add_repeat_no);
        FormEntry.clickGoToIconInForm();
        onView(withId(R.id.list)).check(ViewAssertions.matches(RecyclerViewMatcher.withListSize(3)));
        FormEntry.clickOnText("Group 1");
        onView(withId(R.id.list)).check(ViewAssertions.matches(RecyclerViewMatcher.withListSize(3)));
        FormEntry.checkIfTextDoesNotExist("Repeat Group 1");
    }

    @Test
    //https://github.com/opendatakit/collect/issues/2942
    public void test2() {
        FormEntry.swipeToNextQuestion();
        FormEntry.swipeToNextQuestion();
        FormEntry.swipeToNextQuestion();
        FormEntry.swipeToNextQuestion();
        FormEntry.swipeToNextQuestion();
        FormEntry.swipeToNextQuestion();
        FormEntry.clickOnString(R.string.add_another);
        FormEntry.swipeToNextQuestion();
        FormEntry.clickOnString(R.string.add_another);
        FormEntry.swipeToNextQuestion();
        FormEntry.clickOnString(R.string.add_another);
        FormEntry.swipeToNextQuestion();
        FormEntry.clickOnString(R.string.add_repeat_no);
        FormEntry.clickOnString(R.string.add_repeat_no);
        FormEntry.clickGoToIconInForm();
        FormEntry.clickOnText("Repeat Group 1");
        FormEntry.clickOnText("Repeat Group 1 > 1");
        FormEntry.clickOnText("Repeat Group 1_1");
        FormEntry.clickOnText("Repeat Group 1_1 > 2");
        FormEntry.clickDeleteChildIcon();
        FormEntry.clickOnString(R.string.delete_repeat);
        onView(withId(R.id.list)).check(ViewAssertions.matches(RecyclerViewMatcher.withListSize(1)));
        FormEntry.checkIsTextDisplayed("Repeat Group 1_1 > 1");
    }
}
