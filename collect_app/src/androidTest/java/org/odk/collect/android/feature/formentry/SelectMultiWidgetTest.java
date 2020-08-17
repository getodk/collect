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
