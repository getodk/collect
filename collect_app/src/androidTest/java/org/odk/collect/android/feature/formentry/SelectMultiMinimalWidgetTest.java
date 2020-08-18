package org.odk.collect.android.feature.formentry;

import android.Manifest;

import androidx.test.espresso.intent.rule.IntentsTestRule;
import androidx.test.rule.GrantPermissionRule;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.odk.collect.android.activities.FormEntryActivity;
import org.odk.collect.android.support.CopyFormRule;
import org.odk.collect.android.support.FormLoadingUtils;
import org.odk.collect.android.support.ResetStateRule;
import org.odk.collect.android.support.pages.FormEntryPage;

import static java.util.Arrays.asList;

public class SelectMultiMinimalWidgetTest {
    @Rule
    public IntentsTestRule<FormEntryActivity> activityTestRule = FormLoadingUtils.getFormActivityTestRuleFor("select_multi_minimal_form.xml");

    @Rule
    public RuleChain copyFormChain = RuleChain
            .outerRule(GrantPermissionRule.grant(
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)
            )
            .around(new ResetStateRule())
            .around(new CopyFormRule("select_multi_minimal_form.xml",
                    asList("selectsForm-media/a.png",
                            "selectsForm-media/b.png",
                            "selectsForm-media/c.png",
                            "selectsForm-media/a.wav",
                            "selectsForm-media/b.wav",
                            "selectsForm-media/c.wav",
                            "selectsForm-media/a.mp4",
                            "selectsForm-media/a.mp4",
                            "selectsForm-media/a.mp4"), true));

    @Test
    public void whenAutocompleteAppearanceIsNotUsed_shouldSearchBoxBeHidden() {
        new FormEntryPage("select_multi_minimal_form", activityTestRule)
                .openSelectMinimalDialog()
                .assertSearchBoxIsHidden(true)
                .closeSelectMinimalDialog()
                .swipeToNextQuestion("Q2")
                .openSelectMinimalDialog()
                .assertSearchBoxIsVisible(true)
                .closeSelectMinimalDialog()
                .swipeToNextQuestion("Q3")
                .openSelectMinimalDialog()
                .assertSearchBoxIsVisible(true)
                .closeSelectMinimalDialog()
                .swipeToNextQuestion("Q4")
                .openSelectMinimalDialog()
                .assertSearchBoxIsHidden(true);
    }

    @Test
    public void whenSelectingLongLabel_shouldEntireAnswerBeDisplayed() {
        new FormEntryPage("select_multi_minimal_form", activityTestRule)
                .clickGoToArrow()
                .clickOnQuestion("Q5")
                .openSelectMinimalDialog()
                .assertText("Integer a eleifend libero, sit amet tincidunt lacus. Donec orci tellus, facilisis et ultricies vel")
                .assertText("Nam varius, lectus non consectetur tincidunt, augue augue dapibus dolor, nec convallis ligula erat eget")
                .clickOnText("Integer a eleifend libero, sit amet tincidunt lacus. Donec orci tellus, facilisis et ultricies vel")
                .closeSelectMinimalDialog()
                .assertText("Integer a eleifend libero, sit amet tincidunt lacus. Donec orci tellus, facilisis et ultricies vel");
    }

    @Test
    public void bothClickingBackArrowIconAndDeviceBackButton_shouldCloseDialogAndSaveAnswers() {
        new FormEntryPage("select_multi_minimal_form", activityTestRule)
                .openSelectMinimalDialog()
                .clickOnText("BBB")
                .closeSelectMinimalDialog()
                .assertText("BBB")
                .openSelectMinimalDialog()
                .clickOnText("BBB")
                .clickOnText("AAA")
                .pressBack(new FormEntryPage("select_multi_minimal_form", activityTestRule))
                .assertText("AAA");
    }
}
