package org.odk.collect.android.feature.formentry;

import android.Manifest;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.rule.GrantPermissionRule;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.runner.RunWith;
import org.odk.collect.android.R;
import org.odk.collect.android.support.CollectTestRule;
import org.odk.collect.android.support.CopyFormRule;
import org.odk.collect.android.support.ResetStateRule;
import org.odk.collect.android.support.pages.EndOfFormPage;
import org.odk.collect.android.support.pages.FormEntryPage;
import org.odk.collect.android.support.pages.MainMenuPage;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.doesNotExist;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

@RunWith(AndroidJUnit4.class)
public class AddRepeatTest {

    private static final String ONE_QUESTION_REPEAT = "one-question-repeat.xml";
    private static final String FIELD_LIST_REPEAT = "field-list-repeat.xml";
    private static final String FIXED_COUNT_REPEAT = "fixed-count-repeat.xml";

    private final CollectTestRule rule = new CollectTestRule();

    @Rule
    public RuleChain copyFormChain = RuleChain
            .outerRule(GrantPermissionRule.grant(Manifest.permission.READ_PHONE_STATE))
            .around(new ResetStateRule())
            .around(new CopyFormRule(ONE_QUESTION_REPEAT))
            .around(new CopyFormRule(FIELD_LIST_REPEAT))
            .around(new CopyFormRule(FIXED_COUNT_REPEAT))
            .around(rule);

    @Test
    public void whenInRepeat_swipingNext_andClickingAdd_addsAnotherRepeat() {
        new MainMenuPage()
                .startBlankForm("One Question Repeat")
                .assertText("Person > 1")
                .swipeToNextQuestionWithRepeatGroup("Person")
                .clickOnAdd(new FormEntryPage("One Question Repeat"))
                .assertText("Person > 2");
    }

    @Test
    public void whenInRepeat_swipingNext_andClickingDoNotAdd_leavesRepeatGroup() {
        new MainMenuPage()
                .startBlankForm("One Question Repeat")
                .assertText("Person > 1")
                .swipeToNextQuestionWithRepeatGroup("Person")
                .clickOnDoNotAdd(new EndOfFormPage("One Question Repeat"));
    }

    @Test
    public void whenInRepeat_thatIsAFieldList_swipingNext_andClickingAdd_addsAnotherRepeat() {
        new MainMenuPage()
                .startBlankForm("Field-List Repeat")
                .assertText("Person > 1")
                .assertText("What is their age?")
                .assertText("What is their name?")
                .swipeToNextQuestionWithRepeatGroup("Person")
                .clickOnAdd(new FormEntryPage("One Question Repeat"))
                .assertText("Person > 2")
                .assertText("What is their age?")
                .assertText("What is their name?");
    }

    @Test
    public void whenInRepeat_clickingPlus_andClickingAdd_addsRepeatToEndOfSeries() {
        new MainMenuPage()
                .startBlankForm("One Question Repeat")
                .assertText("Person > 1")
                .swipeToNextQuestionWithRepeatGroup("Person")
                .clickOnAdd(new FormEntryPage("One Question Repeat"))
                .swipeToPreviousQuestion()
                .assertText("Person > 1")
                .clickPlus("Person")
                .clickOnAdd(new FormEntryPage("One Question Repeat"))
                .assertText("Person > 3");
    }

    @Test
    public void whenInARepeat_clickingPlus_andClickingDoNotAdd_returns() {
        new MainMenuPage()
                .startBlankForm("One Question Repeat")
                .assertText("Person > 1")
                .swipeToNextQuestionWithRepeatGroup("Person")
                .clickOnAdd(new FormEntryPage("One Question Repeat"))
                .swipeToPreviousQuestion()
                .assertText("Person > 1")
                .clickPlus("Person")
                .clickOnDoNotAdd(new FormEntryPage("One Question Repeat"))
                .assertText("Person > 1");
    }

    @Test
    public void whenInRepeatWithFixedCount_noPlusButtonAppears() {
        new MainMenuPage()
                .startBlankForm("Fixed Count Repeat");

        onView(withId(R.id.menu_add_repeat)).check(doesNotExist());
    }

    @Test
    public void whenInHierarchyForRepeat_clickingPlus_addsRepeatAtEndOfSeries() {
        new MainMenuPage()
                .startBlankForm("One Question Repeat")
                .assertText("Person > 1")
                .swipeToNextQuestionWithRepeatGroup("Person")
                .clickOnAdd(new FormEntryPage("One Question Repeat"))
                .clickGoToArrow()
                .clickGoUpIcon()
                .addGroup()
                .assertText("Person > 3");
    }
}
