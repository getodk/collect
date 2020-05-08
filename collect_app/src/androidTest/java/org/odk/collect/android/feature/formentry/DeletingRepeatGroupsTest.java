package org.odk.collect.android.feature.formentry;

import android.Manifest;

import androidx.test.espresso.intent.rule.IntentsTestRule;
import androidx.test.rule.GrantPermissionRule;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.odk.collect.android.R;
import org.odk.collect.android.activities.FormEntryActivity;
import org.odk.collect.android.support.pages.FormEntryPage;
import org.odk.collect.android.support.CopyFormRule;
import org.odk.collect.android.support.ResetStateRule;
import org.odk.collect.android.support.matchers.RecyclerViewMatcher;
import org.odk.collect.android.support.FormLoadingUtils;
import org.odk.collect.android.support.pages.FormHierarchyPage;

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
            .around(new CopyFormRule(TEST_FORM, true));

    @Test
    public void requestingDeletionOfFirstRepeat_deletesFirstRepeat() {
        new FormEntryPage("repeatGroups", activityTestRule)
                .deleteGroup("text1")
                .assertText("2");
    }

    @Test
    public void requestingDeletionOfMiddleRepeat_deletesMiddleRepeat() {
        new FormEntryPage("repeatGroups", activityTestRule)
                .swipeToNextQuestion()
                .deleteGroup("text1")
                .assertText("3");
    }

    @Test
    public void requestingDeletionOfLastRepeat_deletesLastRepeat() {
        new FormEntryPage("repeatGroups", activityTestRule)
                .swipeToNextQuestion()
                .swipeToNextQuestion()
                .swipeToNextQuestion()
                .deleteGroup("text1")
                .assertText("number1");
    }

    @Test
    public void requestingDeletionOfFirstRepeatInHierarchy_deletesFirstRepeat() {
        FormHierarchyPage page = new FormEntryPage("repeatGroups", activityTestRule)
                .clickGoToArrow()
                .clickGoUpIcon();

        onView(withId(R.id.list)).check(matches(RecyclerViewMatcher.withListSize(4)));

        page.clickOnText("repeatGroup > 1")
                .assertText("1")
                .deleteGroup();

        onView(withId(R.id.list)).check(matches(RecyclerViewMatcher.withListSize(3)));

        page.clickOnText("repeatGroup > 1")
                .assertText("2");
    }

    @Test
    public void requestingDeletionOfMiddleRepeatInHierarchy_deletesMiddleRepeat() {
        FormHierarchyPage page = new FormEntryPage("repeatGroups", activityTestRule)
                .clickGoToArrow()
                .clickGoUpIcon();

        onView(withId(R.id.list)).check(matches(RecyclerViewMatcher.withListSize(4)));

        page.clickOnText("repeatGroup > 2")
                .assertText("2")
                .deleteGroup();

        onView(withId(R.id.list)).check(matches(RecyclerViewMatcher.withListSize(3)));

        page.clickOnText("repeatGroup > 2")
                .assertText("3");
    }

    @Test
    public void requestingDeletionOfLastRepeatInHierarchy_deletesLastRepeat() {
        FormHierarchyPage page = new FormEntryPage("repeatGroups", activityTestRule)
                .clickGoToArrow()
                .clickGoUpIcon();

        onView(withId(R.id.list)).check(matches(RecyclerViewMatcher.withListSize(4)));

        page.clickOnText("repeatGroup > 4")
                .assertText("4")
                .deleteGroup();

        onView(withId(R.id.list)).check(matches(RecyclerViewMatcher.withListSize(3)));

        page.clickOnText("repeatGroup > 3")
                .assertText("3");
    }

    @Test
    public void requestingDeletionOfFirstRepeatWithFieldList_deletesFirstRepeat() {
        new FormEntryPage("repeatGroups", activityTestRule)
                .clickGoToArrow()
                .clickGoUpIcon()
                .clickGoUpIcon()
                .clickOnText("repeatGroupFieldList")
                .clickOnText("repeatGroupFieldList > 1")
                .clickOnQuestion("number1")
                .deleteGroup("number1")
                .assertText("2");
    }

    @Test
    public void requestingDeletionOfMiddleRepeatWithFieldList_deletesMiddleRepeat() {
        new FormEntryPage("repeatGroups", activityTestRule)
                .clickGoToArrow()
                .clickGoUpIcon()
                .clickGoUpIcon()
                .clickOnText("repeatGroupFieldList")
                .clickOnText("repeatGroupFieldList > 2")
                .clickOnQuestion("number1")
                .deleteGroup("number1")
                .assertText("3");
    }

    @Test
    public void requestingDeletionOfLastRepeatWithFieldList_deletesLastRepeat() {
        new FormEntryPage("repeatGroups", activityTestRule)
                .clickGoToArrow()
                .clickGoUpIcon()
                .clickGoUpIcon()
                .clickOnText("repeatGroupFieldList")
                .clickOnText("repeatGroupFieldList > 4")
                .clickOnQuestion("number1")
                .deleteGroup("number1")
                .checkIsStringDisplayed(R.string.quit_entry);
    }

    @Test
    public void requestingDeletionOfFirstRepeatWithFieldListInHierarchy_deletesFirstRepeat() {
        FormHierarchyPage page = new FormEntryPage("repeatGroups", activityTestRule)
                .clickGoToArrow()
                .clickGoUpIcon()
                .clickGoUpIcon()
                .clickOnText("repeatGroupFieldList");

        onView(withId(R.id.list)).check(matches(RecyclerViewMatcher.withListSize(4)));

        page.clickOnText("repeatGroupFieldList > 1")
                .deleteGroup();

        onView(withId(R.id.list)).check(matches(RecyclerViewMatcher.withListSize(3)));

        page.clickOnText("repeatGroupFieldList > 1")
                .assertText("2");
    }

    @Test
    public void requestingDeletionOfMiddleRepeatWithFieldListInHierarchy_deletesMiddleRepeat() {
        FormHierarchyPage page = new FormEntryPage("repeatGroups", activityTestRule)
                .clickGoToArrow()
                .clickGoUpIcon()
                .clickGoUpIcon()
                .clickOnText("repeatGroupFieldList");

        onView(withId(R.id.list)).check(matches(RecyclerViewMatcher.withListSize(4)));

        page.clickOnText("repeatGroupFieldList > 2")
                .deleteGroup();
        onView(withId(R.id.list)).check(matches(RecyclerViewMatcher.withListSize(3)));

        page.clickOnText("repeatGroupFieldList > 2")
                .assertText("3");
    }

    @Test
    public void requestingDeletionOfLastRepeatWithFieldListInHierarchy_deletesLastRepeat() {
        FormHierarchyPage page = new FormEntryPage("repeatGroups", activityTestRule)
                .clickGoToArrow()
                .clickGoUpIcon()
                .clickGoUpIcon()
                .clickOnText("repeatGroupFieldList");

        onView(withId(R.id.list)).check(matches(RecyclerViewMatcher.withListSize(4)));

        page.clickOnText("repeatGroupFieldList > 4")
                .deleteGroup();
        onView(withId(R.id.list)).check(matches(RecyclerViewMatcher.withListSize(3)));

        page.clickOnText("repeatGroupFieldList > 3")
                .assertText("3");
    }
}
