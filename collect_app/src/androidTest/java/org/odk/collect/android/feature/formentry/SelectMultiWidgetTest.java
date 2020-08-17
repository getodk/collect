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

public class SelectMultiWidgetTest {
    @Rule
    public IntentsTestRule<FormEntryActivity> activityTestRule = FormLoadingUtils.getFormActivityTestRuleFor("select_multi_form.xml");

    @Rule
    public RuleChain copyFormChain = RuleChain
            .outerRule(GrantPermissionRule.grant(
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)
            )
            .around(new ResetStateRule())
            .around(new CopyFormRule("select_multi_form.xml",
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
    public void assertAllItemsAreDisplayedInVariousAppearances() {
        new FormEntryPage("select_multi_form", activityTestRule)
                .assertItemLabel(0, "AAA")
                .assertItemLabel(1, "BBB")
                .assertItemLabel(2, "CCC")
                .swipeToNextQuestion("Q2")
                .assertItemLabel(0, "AAA")
                .assertItemLabel(1, "BBB")
                .assertItemLabel(2, "CCC")
                .swipeToNextQuestion("Q3")
                .assertItemLabel(0, "AAA")
                .assertItemLabel(1, "BBB")
                .assertItemLabel(2, "CCC")
                .swipeToNextQuestion("Q4")
                .assertItemLabel(0, "AAA")
                .assertItemLabel(1, "BBB")
                .assertItemLabel(2, "CCC");
    }

    @Test
    public void assertFilteringWorksAsExpected() {
         new FormEntryPage("select_multi_form", activityTestRule)
                .clickGoToArrow()
                .clickOnQuestion("Q2")
                .assertItemLabel(0, "AAA")
                .assertItemLabel(1, "BBB")
                .assertItemLabel(2, "CCC")
                .filterChoices("b", false)
                .assertItemLabel(0, "BBB")
                .assertTextDoesNotExist("AAA", "CCC")
                .clickOnText("BBB")
                .assertItemChecked(0, false)
                .filterChoices("bk", false)
                .assertTextDoesNotExist("AAA", "BBB", "CCC")
                .filterChoices("b", false)
                .assertItemLabel(0, "BBB")
                .assertTextDoesNotExist("AAA", "CCC")
                .filterChoices("", false)
                .assertItemLabel(0, "AAA")
                .assertItemLabel(1, "BBB")
                .assertItemLabel(2, "CCC")
                .assertItemChecked(1, false)
                .assertItemNotChecked(0, false)
                .assertItemNotChecked(2, false);
    }

    @Test
    public void assertFilteringWorksAsExpectedInNoButtonsMode() {
        new FormEntryPage("select_multi_form", activityTestRule)
                .clickGoToArrow()
                .clickOnQuestion("Q3")
                .assertItemLabel(0, "AAA")
                .assertItemLabel(1, "BBB")
                .assertItemLabel(2, "CCC")
                .filterChoices("b", false)
                .assertItemLabel(0, "BBB")
                .assertTextDoesNotExist("AAA", "CCC")
                .clickOnText("BBB")
                .assertItemChecked(0, true)
                .filterChoices("bk", false)
                .assertTextDoesNotExist("AAA", "BBB", "CCC")
                .filterChoices("b", false)
                .assertItemLabel(0, "BBB")
                .assertTextDoesNotExist("AAA", "CCC")
                .filterChoices("", false)
                .assertItemLabel(0, "AAA")
                .assertItemLabel(1, "BBB")
                .assertItemLabel(2, "CCC")
                .assertItemChecked(1, true)
                .assertItemNotChecked(0, true)
                .assertItemNotChecked(2, true);
    }

    @Test
    public void whenAutocompleteAppearanceIsNotUsed_shouldSearchBoxBeHidden() {
        new FormEntryPage("select_multi_form", activityTestRule)
                .assertSearchBoxIsHidden(false)
                .swipeToNextQuestion("Q2")
                .assertSearchBoxIsVisible(false)
                .swipeToNextQuestion("Q3")
                .assertSearchBoxIsVisible(false)
                .swipeToNextQuestion("Q4")
                .assertSearchBoxIsHidden(false);
    }

    @Test
    public void whenMediaFilesAreMissing_shouldAppropriateMessageBeDisplayed() {
        new FormEntryPage("select_multi_form", activityTestRule)
                .clickGoToArrow()
                .clickOnQuestion("Q4")
                .assertFileNotFoundMsg("/storage/emulated/0/odk/forms/select_multi_form-media/x.png")
                .assertFileNotFoundMsg("/storage/emulated/0/odk/forms/select_multi_form-media/y.png")
                .assertFileNotFoundMsg("/storage/emulated/0/odk/forms/select_multi_form-media/z.png")
                .clickAudioButton(0)
                .assertFileNotFoundToast("//storage/emulated/0/odk/forms/select_multi_form-media/x.wav")
                .clickVideoButton(0)
                .assertFileNotFoundToast("//storage/emulated/0/odk/forms/select_multi_form-media/x.mp4")
                .clickAudioButton(1)
                .assertFileNotFoundToast("//storage/emulated/0/odk/forms/select_multi_form-media/y.wav")
                .clickVideoButton(1)
                .assertFileNotFoundToast("//storage/emulated/0/odk/forms/select_multi_form-media/y.mp4")
                .clickAudioButton(2)
                .assertFileNotFoundToast("//storage/emulated/0/odk/forms/select_multi_form-media/z.wav")
                .clickVideoButton(2)
                .assertFileNotFoundToast("//storage/emulated/0/odk/forms/select_multi_form-media/z.mp4");
    }

    @Test
    public void clickingOnAlreadySelectedOption_shouldUnselectIt() {
        new FormEntryPage("select_multi_form", activityTestRule)
                .clickOnText("AAA")
                .assertItemChecked(0, false)
                .clickOnText("AAA")
                .assertItemNotChecked(0, false)
                .clickImageButton(1)
                .assertItemChecked(1, false)
                .clickImageButton(1)
                .assertItemNotChecked(1, false);
    }

    @Test
    public void clickingOnAlreadySelectedOption_shouldNotUnselectItInNoButtonsMode() {
        new FormEntryPage("select_multi_form", activityTestRule)
                .clickGoToArrow()
                .clickOnQuestion("Q3")
                .clickOnText("AAA")
                .assertItemChecked(0, true)
                .clickOnText("AAA")
                .assertItemNotChecked(0, true);
    }
}
