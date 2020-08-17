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

public class SelectOneWidgetTest {
    @Rule
    public IntentsTestRule<FormEntryActivity> activityTestRule = FormLoadingUtils.getFormActivityTestRuleFor("select_one_form.xml");

    @Rule
    public RuleChain copyFormChain = RuleChain
            .outerRule(GrantPermissionRule.grant(
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)
            )
            .around(new ResetStateRule())
            .around(new CopyFormRule("select_one_form.xml",
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
    public void whenQuickAppearanceExist_shouldNavigateForwardAfterSelectingAnswer() {
        new FormEntryPage("select_one_form", activityTestRule)
                .swipeToNextQuestion("Q2")
                .clickOnText("AAA")
                .assertQuestion("Q3");
    }

    @Test
    public void clickingOnAlreadySelectedOption_shouldNotUnselectIt() {
        new FormEntryPage("select_one_form", activityTestRule)
                .clickOnText("AAA")
                .assertItemChecked(0, false)
                .clickOnText("AAA")
                .assertItemChecked(0, false)
                .clickImageButton(1)
                .assertItemChecked(1, false)
                .clickImageButton(1)
                .assertItemChecked(1, false);
    }

    @Test
    public void clickingOnAlreadySelectedOption_shouldNotUnselectItInNoButtonsMode() {
        new FormEntryPage("select_one_form", activityTestRule)
                .clickGoToArrow()
                .clickOnQuestion("Q4")
                .clickOnText("AAA")
                .assertItemChecked(0, true)
                .clickOnText("AAA")
                .assertItemChecked(0, true);
    }
}
