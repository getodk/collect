package org.odk.collect.android.feature.formentry;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.odk.collect.android.R;
import org.odk.collect.android.support.AdbFormLoadingUtils;
import org.odk.collect.android.support.CopyFormRule;
import org.odk.collect.android.support.FormActivityTestRule;
import org.odk.collect.android.support.ResetStateRule;
import org.odk.collect.android.support.matchers.RecyclerViewMatcher;
import org.odk.collect.android.support.pages.FormEndPage;
import org.odk.collect.android.support.pages.FormEntryPage;
import org.odk.collect.android.support.pages.FormHierarchyPage;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

public class DeletingRepeatGroupsTest {
    private static final String TEST_FORM = "repeat_groups.xml";

    private final FormActivityTestRule activityTestRule = AdbFormLoadingUtils.getFormActivityTestRuleFor(TEST_FORM);

    @Rule
    public RuleChain copyFormChain = RuleChain
            .outerRule(new ResetStateRule())
            .around(new CopyFormRule(TEST_FORM, true))
            .around(activityTestRule);

    @Test
    public void requestingDeletionOfFirstRepeat_deletesFirstRepeat() {
        new FormEntryPage("repeatGroups")
                .swipeToNextQuestion("text1")
                .deleteGroup("text1")
                .assertText("2");
    }

    @Test
    public void requestingDeletionOfMiddleRepeat_deletesMiddleRepeat() {
        new FormEntryPage("repeatGroups")
                .swipeToNextQuestion("text1")
                .swipeToNextRepeat("repeatGroup", 2)
                .deleteGroup("text1")
                .assertText("3");
    }

    @Test
    public void requestingDeletionOfLastRepeat_deletesLastRepeat() {
        new FormEntryPage("repeatGroups")
                .swipeToNextQuestion("text1")
                .swipeToNextRepeat("repeatGroup", 2)
                .swipeToNextRepeat("repeatGroup", 3)
                .swipeToNextRepeat("repeatGroup", 4)
                .deleteGroup("text1")
                .assertText("number1");
    }

    @Test
    public void requestingDeletionOfFirstRepeatInHierarchy_deletesFirstRepeat() {
        FormHierarchyPage page = new FormEntryPage("repeatGroups")
                .swipeToNextQuestion("text1")
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
        FormHierarchyPage page = new FormEntryPage("repeatGroups")
                .swipeToNextQuestion("text1")
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
        FormHierarchyPage page = new FormEntryPage("repeatGroups")
                .swipeToNextQuestion("text1")
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
    public void requestingDeletionOfAllRepeatsInHierarchyStartingFromIndexThatWillBeDeleted_shouldBringAUserToTheFirstRelevantQuestionBeforeTheGroup() {
        new FormEntryPage("repeatGroups")
                .swipeToNextQuestion("text1")
                .clickGoToArrow()
                .clickGoUpIcon()
                .clickOnText("repeatGroup > 4")
                .deleteGroup()
                .clickOnText("repeatGroup > 3")
                .deleteGroup()
                .clickOnText("repeatGroup > 2")
                .deleteGroup()
                .clickOnText("repeatGroup > 1")
                .deleteGroup()
                .pressBack(new FormEntryPage("repeatGroups"))
                .assertText("text0");
    }

    @Test
    public void requestingDeletionOfAllRepeatsInHierarchyStartingFromIndexThatWillNotBeDeleted_shouldBringAUserBackToTheSameIndex() {
        new FormEntryPage("repeatGroups")
                .clickGoToArrow()
                .clickOnText("repeatGroup")
                .clickOnText("repeatGroup > 4")
                .deleteGroup()
                .clickOnText("repeatGroup > 3")
                .deleteGroup()
                .clickOnText("repeatGroup > 2")
                .deleteGroup()
                .clickOnText("repeatGroup > 1")
                .deleteGroup()
                .pressBack(new FormEntryPage("repeatGroups"))
                .assertText("text0");
    }

    @Test
    public void requestingDeletionOfAllRepeatsInHierarchyStartingFromTheEndView_shouldBringAUserToTheEndView() {
        new FormEntryPage("repeatGroups")
                .clickGoToArrow()
                .clickJumpEndButton()
                .clickGoToArrow()
                .clickOnText("repeatGroup")
                .clickOnText("repeatGroup > 4")
                .deleteGroup()
                .clickOnText("repeatGroup > 3")
                .deleteGroup()
                .clickOnText("repeatGroup > 2")
                .deleteGroup()
                .clickOnText("repeatGroup > 1")
                .deleteGroup()
                .pressBack(new FormEndPage("repeatGroups"));
    }

    @Test
    public void requestingDeletionOfFirstRepeatWithFieldList_deletesFirstRepeat() {
        new FormEntryPage("repeatGroups")
                .swipeToNextQuestion("text1")
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
        new FormEntryPage("repeatGroups")
                .swipeToNextQuestion("text1")
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
        new FormEntryPage("repeatGroups")
                .swipeToNextQuestion("text1")
                .clickGoToArrow()
                .clickGoUpIcon()
                .clickGoUpIcon()
                .clickOnText("repeatGroupFieldList")
                .clickOnText("repeatGroupFieldList > 4")
                .clickOnQuestion("number1")
                .deleteGroup("number1")
                .assertText(R.string.quit_entry);
    }

    @Test
    public void requestingDeletionOfFirstRepeatWithFieldListInHierarchy_deletesFirstRepeat() {
        FormHierarchyPage page = new FormEntryPage("repeatGroups")
                .swipeToNextQuestion("text1")
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
        FormHierarchyPage page = new FormEntryPage("repeatGroups")
                .swipeToNextQuestion("text1")
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
        FormHierarchyPage page = new FormEntryPage("repeatGroups")
                .swipeToNextQuestion("text1")
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

    @Test
    public void requestingDeletionOfAllRepeatsWithFieldListInHierarchyStartingFromIndexThatWillBeDeleted_shouldBringAUserToTheFirstRelevantQuestionBeforeTheGroup() {
        new FormEntryPage("repeatGroups")
                .swipeToNextQuestion("text1")
                .clickGoToArrow()
                .clickGoUpIcon()
                .clickGoUpIcon()
                .clickOnText("repeatGroupFieldList")
                .clickOnText("repeatGroupFieldList > 1")
                .clickOnQuestion("number1")
                .clickGoToArrow()
                .clickGoUpIcon()
                .clickOnText("repeatGroupFieldList > 4")
                .deleteGroup()
                .clickOnText("repeatGroupFieldList > 3")
                .deleteGroup()
                .clickOnText("repeatGroupFieldList > 2")
                .deleteGroup()
                .clickOnText("repeatGroupFieldList > 1")
                .deleteGroup()
                .pressBack(new FormEntryPage("repeatGroups"))
                .assertText("repeatGroup > 4");
    }

    @Test
    public void requestingDeletionOfAllRepeatsWithFieldListInHierarchyStartingFromIndexThatWillNotBeDeleted_shouldBringAUserBackToTheSameIndex() {
        new FormEntryPage("repeatGroups")
                .clickGoToArrow()
                .clickOnText("repeatGroupFieldList")
                .clickOnText("repeatGroupFieldList > 4")
                .deleteGroup()
                .clickOnText("repeatGroupFieldList > 3")
                .deleteGroup()
                .clickOnText("repeatGroupFieldList > 2")
                .deleteGroup()
                .clickOnText("repeatGroupFieldList > 1")
                .deleteGroup()
                .pressBack(new FormEntryPage("repeatGroups"))
                .assertText("text0");
    }

    @Test
    public void requestingDeletionOfAllRepeatsWithFieldListInHierarchyStartingFromTheEndView_shouldBringAUserToTheEndView() {
        new FormEntryPage("repeatGroups")
                .clickGoToArrow()
                .clickJumpEndButton()
                .clickGoToArrow()
                .clickOnText("repeatGroupFieldList")
                .clickOnText("repeatGroupFieldList > 4")
                .deleteGroup()
                .clickOnText("repeatGroupFieldList > 3")
                .deleteGroup()
                .clickOnText("repeatGroupFieldList > 2")
                .deleteGroup()
                .clickOnText("repeatGroupFieldList > 1")
                .deleteGroup()
                .pressBack(new FormEndPage("repeatGroups"));
    }
}
