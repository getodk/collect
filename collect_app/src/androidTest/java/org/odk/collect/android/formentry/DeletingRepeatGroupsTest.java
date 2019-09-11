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
import org.odk.collect.android.support.matchers.RecyclerViewMatcher;
import org.odk.collect.android.test.FormLoadingUtils;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

public class DeletingRepeatGroupsTest {
    private static final String TEST_FORM = "repeat_groups.xml";

    @Rule
    public IntentsTestRule<FormEntryActivity> activityTestRule = FormLoadingUtils.getFormActivityTestRuleFor(TEST_FORM);

    @Rule
    public RuleChain copyFormChain = RuleChain
            .outerRule(GrantPermissionRule.grant(
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                    )
            )
            .around(new ResetStateRule())
            .around(new CopyFormRule(TEST_FORM, null));

    @Test
    public void requestingDeletionOfFirstRepeat_deletesFirstRepeat() {
        FormEntry.deleteGroup("text1");
        FormEntry.checkIsTextDisplayed("2");
    }

    @Test
    public void requestingDeletionOfMiddleRepeat_deletesMiddleRepeat() {
        FormEntry.swipeToNextQuestion();
        FormEntry.deleteGroup("text1");
        FormEntry.checkIsTextDisplayed("3");
    }

    @Test
    public void requestingDeletionOfLastRepeat_deletesLastRepeat() {
        FormEntry.swipeToNextQuestion();
        FormEntry.swipeToNextQuestion();
        FormEntry.swipeToNextQuestion();
        FormEntry.deleteGroup("text1");
        FormEntry.checkIsTextDisplayed("number1");
    }

    @Test
    public void requestingDeletionOfFirstRepeatInHierarchy_deletesFirstRepeat() {
        FormEntry.clickGoToIconInForm();
        FormEntry.clickGoUpIcon();
        onView(withId(R.id.list)).check(matches(RecyclerViewMatcher.withListSize(4)));
        FormEntry.clickOnText("repeatGroup > 1");
        FormEntry.checkIsTextDisplayed("1");
        FormEntry.deleteGroup();
        onView(withId(R.id.list)).check(matches(RecyclerViewMatcher.withListSize(3)));
        FormEntry.clickOnText("repeatGroup > 1");
        FormEntry.checkIsTextDisplayed("2");
    }

    @Test
    public void requestingDeletionOfMiddleRepeatInHierarchy_deletesMiddleRepeat() {
        FormEntry.clickGoToIconInForm();
        FormEntry.clickGoUpIcon();
        onView(withId(R.id.list)).check(matches(RecyclerViewMatcher.withListSize(4)));
        FormEntry.clickOnText("repeatGroup > 2");
        FormEntry.checkIsTextDisplayed("2");
        FormEntry.deleteGroup();
        onView(withId(R.id.list)).check(matches(RecyclerViewMatcher.withListSize(3)));
        FormEntry.clickOnText("repeatGroup > 2");
        FormEntry.checkIsTextDisplayed("3");
    }

    @Test
    public void requestingDeletionOfLastRepeatInHierarchy_deletesLastRepeat() {
        FormEntry.clickGoToIconInForm();
        FormEntry.clickGoUpIcon();
        onView(withId(R.id.list)).check(matches(RecyclerViewMatcher.withListSize(4)));
        FormEntry.clickOnText("repeatGroup > 4");
        FormEntry.checkIsTextDisplayed("4");
        FormEntry.deleteGroup();
        onView(withId(R.id.list)).check(matches(RecyclerViewMatcher.withListSize(3)));
        FormEntry.clickOnText("repeatGroup > 3");
        FormEntry.checkIsTextDisplayed("3");
    }

    @Test
    public void requestingDeletionOfFirstRepeatWithFieldList_deletesFirstRepeat() {
        FormEntry.clickGoToIconInForm();
        FormEntry.clickGoUpIcon();
        FormEntry.clickGoUpIcon();
        FormEntry.clickOnText("repeatGroupFieldList");
        FormEntry.clickOnText("repeatGroupFieldList > 1");
        FormEntry.clickOnText("number1");
        FormEntry.deleteGroup("number1");
        FormEntry.checkIsTextDisplayed("2");
    }

    @Test
    public void requestingDeletionOfMiddleRepeatWithFieldList_deletesMiddleRepeat() {
        FormEntry.clickGoToIconInForm();
        FormEntry.clickGoUpIcon();
        FormEntry.clickGoUpIcon();
        FormEntry.clickOnText("repeatGroupFieldList");
        FormEntry.clickOnText("repeatGroupFieldList > 2");
        FormEntry.clickOnText("number1");
        FormEntry.deleteGroup("number1");
        FormEntry.checkIsTextDisplayed("3");
    }

    @Test
    public void requestingDeletionOfLastRepeatWithFieldList_deletesLastRepeat() {
        FormEntry.clickGoToIconInForm();
        FormEntry.clickGoUpIcon();
        FormEntry.clickGoUpIcon();
        FormEntry.clickOnText("repeatGroupFieldList");
        FormEntry.clickOnText("repeatGroupFieldList > 4");
        FormEntry.clickOnText("number1");
        FormEntry.deleteGroup("number1");
        FormEntry.checkIsStringDisplayed(R.string.quit_entry);
    }

    @Test
    public void requestingDeletionOfFirstRepeatWithFieldListInHierarchy_deletesFirstRepeat() {
        FormEntry.clickGoToIconInForm();
        FormEntry.clickGoUpIcon();
        FormEntry.clickGoUpIcon();
        FormEntry.clickOnText("repeatGroupFieldList");
        onView(withId(R.id.list)).check(matches(RecyclerViewMatcher.withListSize(4)));
        FormEntry.clickOnText("repeatGroupFieldList > 1");
        FormEntry.deleteGroup();
        onView(withId(R.id.list)).check(matches(RecyclerViewMatcher.withListSize(3)));
        FormEntry.clickOnText("repeatGroupFieldList > 1");
        FormEntry.checkIsTextDisplayed("2");
    }

    @Test
    public void requestingDeletionOfMiddleRepeatWithFieldListInHierarchy_deletesMiddleRepeat() {
        FormEntry.clickGoToIconInForm();
        FormEntry.clickGoUpIcon();
        FormEntry.clickGoUpIcon();
        FormEntry.clickOnText("repeatGroupFieldList");
        onView(withId(R.id.list)).check(matches(RecyclerViewMatcher.withListSize(4)));
        FormEntry.clickOnText("repeatGroupFieldList > 2");
        FormEntry.deleteGroup();
        onView(withId(R.id.list)).check(matches(RecyclerViewMatcher.withListSize(3)));
        FormEntry.clickOnText("repeatGroupFieldList > 2");
        FormEntry.checkIsTextDisplayed("3");
    }

    @Test
    public void requestingDeletionOfLastRepeatWithFieldListInHierarchy_deletesLastRepeat() {
        FormEntry.clickGoToIconInForm();
        FormEntry.clickGoUpIcon();
        FormEntry.clickGoUpIcon();
        FormEntry.clickOnText("repeatGroupFieldList");
        onView(withId(R.id.list)).check(matches(RecyclerViewMatcher.withListSize(4)));
        FormEntry.clickOnText("repeatGroupFieldList > 4");
        FormEntry.deleteGroup();
        onView(withId(R.id.list)).check(matches(RecyclerViewMatcher.withListSize(3)));
        FormEntry.clickOnText("repeatGroupFieldList > 3");
        FormEntry.checkIsTextDisplayed("3");
    }
}
