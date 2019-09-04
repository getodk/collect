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
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.longClick;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

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
    public void testDeletingRepeatGroupsInForm() {
        FormEntry.clickOnString(R.string.add_another);
        FormEntry.putText("11");
        FormEntry.swipeToNextQuestion();
        FormEntry.putText("12");
        FormEntry.swipeToNextQuestion();
        FormEntry.clickOnString(R.string.add_another);
        FormEntry.putText("21");
        FormEntry.swipeToNextQuestion();
        FormEntry.putText("22");
        FormEntry.swipeToNextQuestion();
        FormEntry.clickOnString(R.string.add_another);
        FormEntry.putText("31");
        FormEntry.swipeToNextQuestion();
        FormEntry.putText("32");
        FormEntry.swipeToNextQuestion();
        FormEntry.clickOnString(R.string.add_another);
        FormEntry.putText("41");
        FormEntry.swipeToNextQuestion();
        FormEntry.putText("42");
        FormEntry.swipeToPreviousQuestion();
        FormEntry.swipeToPreviousQuestion();
        FormEntry.swipeToPreviousQuestion();
        FormEntry.swipeToPreviousQuestion();
        FormEntry.swipeToPreviousQuestion();
        FormEntry.swipeToPreviousQuestion();
        FormEntry.swipeToPreviousQuestion();
        onView(withText("text1")).perform(longClick());
        onView(withText(R.string.delete_repeat)).perform(click());
        onView(withText(R.string.discard_group)).perform(click());
        FormEntry.checkIsTextDisplayed("21");
        FormEntry.swipeToNextQuestion();
        FormEntry.checkIsTextDisplayed("22");
        FormEntry.swipeToNextQuestion();
        FormEntry.checkIsTextDisplayed("31");
        FormEntry.swipeToNextQuestion();
        FormEntry.checkIsTextDisplayed("32");
        FormEntry.swipeToNextQuestion();
        FormEntry.checkIsTextDisplayed("41");
        FormEntry.swipeToNextQuestion();
        FormEntry.checkIsTextDisplayed("42");
        FormEntry.swipeToPreviousQuestion();
        FormEntry.swipeToPreviousQuestion();
        onView(withText("text2")).perform(longClick());
        onView(withText(R.string.delete_repeat)).perform(click());
        onView(withText(R.string.discard_group)).perform(click());
        FormEntry.checkIsTextDisplayed("41");
        FormEntry.swipeToNextQuestion();
        FormEntry.checkIsTextDisplayed("42");
        onView(withText("text2")).perform(longClick());
        onView(withText(R.string.delete_repeat)).perform(click());
        onView(withText(R.string.discard_group)).perform(click());
        FormEntry.clickOnString(R.string.add_repeat_no);
        FormEntry.swipeToPreviousQuestion();
        FormEntry.checkIsTextDisplayed("22");
        FormEntry.swipeToPreviousQuestion();
        FormEntry.checkIsTextDisplayed("21");
        onView(withText("text1")).perform(longClick());
        onView(withText(R.string.delete_repeat)).perform(click());
        onView(withText(R.string.discard_group)).perform(click());
        FormEntry.checkIsStringDisplayed(R.string.entering_repeat_ask);
    }

    @Test
    public void testDeletingRepeatGroupsInHierarchy() {
        FormEntry.clickOnString(R.string.add_another);
        FormEntry.putText("11");
        FormEntry.swipeToNextQuestion();
        FormEntry.putText("12");
        FormEntry.swipeToNextQuestion();
        FormEntry.clickOnString(R.string.add_another);
        FormEntry.putText("21");
        FormEntry.swipeToNextQuestion();
        FormEntry.putText("22");
        FormEntry.swipeToNextQuestion();
        FormEntry.clickOnString(R.string.add_another);
        FormEntry.putText("31");
        FormEntry.swipeToNextQuestion();
        FormEntry.putText("32");
        FormEntry.swipeToNextQuestion();
        FormEntry.clickOnString(R.string.add_another);
        FormEntry.putText("41");
        FormEntry.swipeToNextQuestion();
        FormEntry.putText("42");
        FormEntry.clickGoToIconInForm();
        FormEntry.checkIsTextDisplayed("41");
        FormEntry.checkIsTextDisplayed("42");
        FormEntry.clickGoUpIcon();
        onView(withId(R.id.list)).check(matches(RecyclerViewMatcher.withListSize(4)));
        FormEntry.clickOnText("repeatGroup > 1");
        FormEntry.checkIsTextDisplayed("11");
        FormEntry.checkIsTextDisplayed("12");
        FormEntry.clickDeleteChildIcon();
        FormEntry.clickOnString(R.string.delete_repeat);
        onView(withId(R.id.list)).check(matches(RecyclerViewMatcher.withListSize(3)));
        FormEntry.clickOnText("repeatGroup > 1");
        FormEntry.checkIsTextDisplayed("21");
        FormEntry.checkIsTextDisplayed("22");
        FormEntry.clickGoUpIcon();
        FormEntry.clickOnText("repeatGroup > 2");
        FormEntry.checkIsTextDisplayed("31");
        FormEntry.checkIsTextDisplayed("32");
        FormEntry.clickGoUpIcon();
        FormEntry.clickOnText("repeatGroup > 3");
        FormEntry.checkIsTextDisplayed("41");
        FormEntry.checkIsTextDisplayed("42");
        FormEntry.clickGoUpIcon();
        FormEntry.clickOnText("repeatGroup > 2");
        FormEntry.clickDeleteChildIcon();
        FormEntry.clickOnString(R.string.delete_repeat);
        onView(withId(R.id.list)).check(matches(RecyclerViewMatcher.withListSize(2)));
        FormEntry.clickOnText("repeatGroup > 1");
        FormEntry.checkIsTextDisplayed("21");
        FormEntry.checkIsTextDisplayed("22");
        FormEntry.clickGoUpIcon();
        FormEntry.clickOnText("repeatGroup > 2");
        FormEntry.checkIsTextDisplayed("41");
        FormEntry.checkIsTextDisplayed("42");
        FormEntry.clickDeleteChildIcon();
        FormEntry.clickOnString(R.string.delete_repeat);
        onView(withId(R.id.list)).check(matches(RecyclerViewMatcher.withListSize(1)));
        FormEntry.clickOnText("repeatGroup > 1");
        FormEntry.checkIsTextDisplayed("21");
        FormEntry.checkIsTextDisplayed("22");
        FormEntry.clickDeleteChildIcon();
        FormEntry.clickOnString(R.string.delete_repeat);
    }

    @Test
    public void testDeletingRepeatGroupsWithFieldListInForm() {
        FormEntry.clickOnString(R.string.add_repeat_no);
        FormEntry.clickOnString(R.string.add_another);
        FormEntry.putTextOnIndex(0, "11");
        FormEntry.putTextOnIndex(1, "12");
        FormEntry.swipeToNextQuestion();
        FormEntry.clickOnString(R.string.add_another);
        FormEntry.putTextOnIndex(0, "21");
        FormEntry.putTextOnIndex(1, "22");
        FormEntry.swipeToNextQuestion();
        FormEntry.clickOnString(R.string.add_another);
        FormEntry.putTextOnIndex(0, "31");
        FormEntry.putTextOnIndex(1, "32");
        FormEntry.swipeToNextQuestion();
        FormEntry.clickOnString(R.string.add_another);
        FormEntry.putTextOnIndex(0, "41");
        FormEntry.putTextOnIndex(1, "42");
        FormEntry.swipeToPreviousQuestion();
        FormEntry.swipeToPreviousQuestion();
        FormEntry.swipeToPreviousQuestion();
        onView(withText("number1")).perform(longClick());
        onView(withText(R.string.delete_repeat)).perform(click());
        onView(withText(R.string.discard_group)).perform(click());
        FormEntry.checkIsTextDisplayed("21");
        FormEntry.checkIsTextDisplayed("22");
        FormEntry.swipeToNextQuestion();
        FormEntry.checkIsTextDisplayed("31");
        FormEntry.checkIsTextDisplayed("32");
        FormEntry.swipeToNextQuestion();
        FormEntry.checkIsTextDisplayed("41");
        FormEntry.checkIsTextDisplayed("42");
        FormEntry.swipeToPreviousQuestion();
        onView(withText("number2")).perform(longClick());
        onView(withText(R.string.delete_repeat)).perform(click());
        onView(withText(R.string.discard_group)).perform(click());
        FormEntry.checkIsTextDisplayed("41");
        FormEntry.checkIsTextDisplayed("42");
        onView(withText("number2")).perform(longClick());
        onView(withText(R.string.delete_repeat)).perform(click());
        onView(withText(R.string.discard_group)).perform(click());
        FormEntry.swipeToPreviousQuestion();
        FormEntry.checkIsTextDisplayed("22");
        FormEntry.checkIsTextDisplayed("21");
        onView(withText("number1")).perform(longClick());
        onView(withText(R.string.delete_repeat)).perform(click());
        onView(withText(R.string.discard_group)).perform(click());
        FormEntry.checkIsStringDisplayed(R.string.quit_entry);
    }

    @Test
    public void testDeletingRepeatGroupsWithFieldListInHierarchy() {
        FormEntry.clickOnString(R.string.add_repeat_no);
        FormEntry.clickOnString(R.string.add_another);
        FormEntry.putTextOnIndex(0, "11");
        FormEntry.putTextOnIndex(1, "12");
        FormEntry.swipeToNextQuestion();
        FormEntry.clickOnString(R.string.add_another);
        FormEntry.putTextOnIndex(0, "21");
        FormEntry.putTextOnIndex(1, "22");
        FormEntry.swipeToNextQuestion();
        FormEntry.clickOnString(R.string.add_another);
        FormEntry.putTextOnIndex(0, "31");
        FormEntry.putTextOnIndex(1, "32");
        FormEntry.swipeToNextQuestion();
        FormEntry.clickOnString(R.string.add_another);
        FormEntry.putTextOnIndex(0, "41");
        FormEntry.putTextOnIndex(1, "42");
        FormEntry.clickGoToIconInForm();
        FormEntry.checkIsTextDisplayed("41");
        FormEntry.checkIsTextDisplayed("42");
        FormEntry.clickGoUpIcon();
        onView(withId(R.id.list)).check(matches(RecyclerViewMatcher.withListSize(4)));
        FormEntry.clickOnText("repeatGroupFieldList > 1");
        FormEntry.checkIsTextDisplayed("11");
        FormEntry.checkIsTextDisplayed("12");
        FormEntry.clickDeleteChildIcon();
        FormEntry.clickOnString(R.string.delete_repeat);
        onView(withId(R.id.list)).check(matches(RecyclerViewMatcher.withListSize(3)));
        FormEntry.clickOnText("repeatGroupFieldList > 1");
        FormEntry.checkIsTextDisplayed("21");
        FormEntry.checkIsTextDisplayed("22");
        FormEntry.clickGoUpIcon();
        FormEntry.clickOnText("repeatGroupFieldList > 2");
        FormEntry.checkIsTextDisplayed("31");
        FormEntry.checkIsTextDisplayed("32");
        FormEntry.clickGoUpIcon();
        FormEntry.clickOnText("repeatGroupFieldList > 3");
        FormEntry.checkIsTextDisplayed("41");
        FormEntry.checkIsTextDisplayed("42");
        FormEntry.clickGoUpIcon();
        FormEntry.clickOnText("repeatGroupFieldList > 2");
        FormEntry.clickDeleteChildIcon();
        FormEntry.clickOnString(R.string.delete_repeat);
        onView(withId(R.id.list)).check(matches(RecyclerViewMatcher.withListSize(2)));
        FormEntry.clickOnText("repeatGroupFieldList > 1");
        FormEntry.checkIsTextDisplayed("21");
        FormEntry.checkIsTextDisplayed("22");
        FormEntry.clickGoUpIcon();
        FormEntry.clickOnText("repeatGroupFieldList > 2");
        FormEntry.checkIsTextDisplayed("41");
        FormEntry.checkIsTextDisplayed("42");
        FormEntry.clickDeleteChildIcon();
        FormEntry.clickOnString(R.string.delete_repeat);
        onView(withId(R.id.list)).check(matches(RecyclerViewMatcher.withListSize(1)));
        FormEntry.clickOnText("repeatGroupFieldList > 1");
        FormEntry.checkIsTextDisplayed("21");
        FormEntry.checkIsTextDisplayed("22");
        FormEntry.clickDeleteChildIcon();
        FormEntry.clickOnString(R.string.delete_repeat);
    }
}
