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

public class SelectOneMinimalWidgetTest {
    @Rule
    public IntentsTestRule<FormEntryActivity> activityTestRule = FormLoadingUtils.getFormActivityTestRuleFor("select_one_minimal_form.xml");

    @Rule
    public RuleChain copyFormChain = RuleChain
            .outerRule(GrantPermissionRule.grant(
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)
            )
            .around(new ResetStateRule())
            .around(new CopyFormRule("select_one_minimal_form.xml",
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
        new FormEntryPage("select_one_minimal_form", activityTestRule)
                .openSelectMinimalDialog()
                .assertItemLabel(0, "AAA")
                .assertItemLabel(1, "BBB")
                .assertItemLabel(2, "CCC")
                .closeSelectMinimalDialog()
                .swipeToNextQuestion("Q2")
                .openSelectMinimalDialog()
                .assertItemLabel(0, "AAA")
                .assertItemLabel(1, "BBB")
                .assertItemLabel(2, "CCC")
                .closeSelectMinimalDialog()
                .swipeToNextQuestion("Q3")
                .openSelectMinimalDialog()
                .assertItemLabel(0, "AAA")
                .assertItemLabel(1, "BBB")
                .assertItemLabel(2, "CCC")
                .closeSelectMinimalDialog()
                .swipeToNextQuestion("Q4")
                .openSelectMinimalDialog()
                .assertItemLabel(0, "AAA")
                .assertItemLabel(1, "BBB")
                .assertItemLabel(2, "CCC")
                .closeSelectMinimalDialog()
                .swipeToNextQuestion("Q5")
                .openSelectMinimalDialog()
                .assertItemLabel(0, "AAA")
                .assertItemLabel(1, "BBB")
                .assertItemLabel(2, "CCC");
    }

    @Test
    public void assertFilteringWorksAsExpected() {
        new FormEntryPage("select_one_minimal_form", activityTestRule)
                .clickGoToArrow()
                .clickOnQuestion("Q3")
                .openSelectMinimalDialog()
                .assertItemLabel(0, "AAA")
                .assertItemLabel(1, "BBB")
                .assertItemLabel(2, "CCC")
                .filterChoices("b", true)
                .assertItemLabel(0, "BBB")
                .assertTextDoesNotExist("AAA", "CCC")
                .filterChoices("bk", true)
                .assertTextDoesNotExist("AAA", "BBB", "CCC")
                .filterChoices("b", true)
                .assertItemLabel(0, "BBB")
                .assertTextDoesNotExist("AAA", "CCC")
                .filterChoices("", true)
                .assertItemLabel(0, "AAA")
                .assertItemLabel(1, "BBB")
                .assertItemLabel(2, "CCC");
    }

    @Test
    public void assertFilteringWorksAsExpectedInNoButtonsMode() {
        new FormEntryPage("select_one_minimal_form", activityTestRule)
                .clickGoToArrow()
                .clickOnQuestion("Q4")
                .openSelectMinimalDialog()
                .assertItemLabel(0, "AAA")
                .assertItemLabel(1, "BBB")
                .assertItemLabel(2, "CCC")
                .filterChoices("b", true)
                .assertItemLabel(0, "BBB")
                .assertTextDoesNotExist("AAA", "CCC")
                .filterChoices("bk", true)
                .assertTextDoesNotExist("AAA", "BBB", "CCC")
                .filterChoices("b", true)
                .assertItemLabel(0, "BBB")
                .assertTextDoesNotExist("AAA", "CCC")
                .filterChoices("", true)
                .assertItemLabel(0, "AAA")
                .assertItemLabel(1, "BBB")
                .assertItemLabel(2, "CCC");
    }

    @Test
    public void whenQuickAppearanceExist_shouldNavigateForwardAfterSelectingAnswer() {
        new FormEntryPage("select_one_minimal_form", activityTestRule)
                .swipeToNextQuestion("Q2")
                .openSelectMinimalDialog()
                .clickOnText("AAA")
                .assertQuestion("Q3");
    }

    @Test
    public void whenAutocompleteAppearanceIsNotUsed_shouldSearchBoxBeHidden() {
        new FormEntryPage("select_one_minimal_form", activityTestRule)
                .openSelectMinimalDialog()
                .assertSearchBoxIsHidden(true)
                .closeSelectMinimalDialog()
                .swipeToNextQuestion("Q2")
                .openSelectMinimalDialog()
                .assertSearchBoxIsHidden(true)
                .closeSelectMinimalDialog()
                .swipeToNextQuestion("Q3")
                .openSelectMinimalDialog()
                .assertSearchBoxIsVisible(true)
                .closeSelectMinimalDialog()
                .swipeToNextQuestion("Q4")
                .openSelectMinimalDialog()
                .assertSearchBoxIsVisible(true)
                .closeSelectMinimalDialog()
                .swipeToNextQuestion("Q5")
                .assertSearchBoxIsHidden(true);
    }

    @Test
    public void whenMediaFilesAreMissing_shouldAppropriateMessageBeDisplayed() {
        new FormEntryPage("select_one_minimal_form", activityTestRule)
                .clickGoToArrow()
                .clickOnQuestion("Q5")
                .openSelectMinimalDialog()
                .assertFileNotFoundMsg("/storage/emulated/0/odk/forms/select_one_minimal_form-media/x.png")
                .assertFileNotFoundMsg("/storage/emulated/0/odk/forms/select_one_minimal_form-media/y.png")
                .assertFileNotFoundMsg("/storage/emulated/0/odk/forms/select_one_minimal_form-media/z.png")
                .clickAudioButton(0)
                .assertFileNotFoundToast("//storage/emulated/0/odk/forms/select_one_minimal_form-media/x.wav")
                .clickVideoButton(0)
                .assertFileNotFoundToast("//storage/emulated/0/odk/forms/select_one_minimal_form-media/x.mp4")
                .clickAudioButton(1)
                .assertFileNotFoundToast("//storage/emulated/0/odk/forms/select_one_minimal_form-media/y.wav")
                .clickVideoButton(1)
                .assertFileNotFoundToast("//storage/emulated/0/odk/forms/select_one_minimal_form-media/y.mp4")
                .clickAudioButton(2)
                .assertFileNotFoundToast("//storage/emulated/0/odk/forms/select_one_minimal_form-media/z.wav")
                .clickVideoButton(2)
                .assertFileNotFoundToast("//storage/emulated/0/odk/forms/select_one_minimal_form-media/z.mp4");
    }

    @Test
    public void bothClickingRadioButtonAndImage_shouldSelectItem() {
        new FormEntryPage("select_one_minimal_form", activityTestRule)
                .openSelectMinimalDialog()
                .clickOnText("AAA")
                .openSelectMinimalDialog()
                .assertItemChecked(0, false)
                .assertItemNotChecked(1, false)
                .assertItemNotChecked(2, false)
                .clickImageButton(1)
                .openSelectMinimalDialog()
                .assertItemNotChecked(0, false)
                .assertItemChecked(1, false)
                .assertItemNotChecked(2, false);
    }

    @Test
    public void clickingOnAlreadySelectedOption_shouldNotUnselectIt() {
        new FormEntryPage("select_one_minimal_form", activityTestRule)
                .openSelectMinimalDialog()
                .clickOnText("AAA")
                .openSelectMinimalDialog()
                .assertItemChecked(0, false)
                .clickOnText("AAA")
                .openSelectMinimalDialog()
                .assertItemChecked(0, false)
                .clickImageButton(1)
                .openSelectMinimalDialog()
                .assertItemChecked(1, false)
                .clickImageButton(1)
                .openSelectMinimalDialog()
                .assertItemChecked(1, false);
    }

    @Test
    public void clickingOnAlreadySelectedOption_shouldNotUnselectItInNoButtonsMode() {
        new FormEntryPage("select_one_minimal_form", activityTestRule)
                .clickGoToArrow()
                .clickOnQuestion("Q4")
                .openSelectMinimalDialog()
                .clickOnText("AAA")
                .openSelectMinimalDialog()
                .assertItemChecked(0, true)
                .clickOnText("AAA")
                .openSelectMinimalDialog()
                .assertItemChecked(0, true);
    }
}
