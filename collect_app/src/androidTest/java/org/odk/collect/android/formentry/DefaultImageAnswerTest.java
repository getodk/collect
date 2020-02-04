package org.odk.collect.android.formentry;

import android.Manifest;

import androidx.test.espresso.intent.rule.IntentsTestRule;
import androidx.test.rule.GrantPermissionRule;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.odk.collect.android.activities.FormEntryActivity;
import org.odk.collect.android.support.CopyFormRule;
import org.odk.collect.android.support.ResetStateRule;
import org.odk.collect.android.support.pages.FormEntryPage;
import org.odk.collect.android.test.FormLoadingUtils;

import java.util.Collections;

public class DefaultImageAnswerTest {

    private static final String TEST_FORM = "default_image_answer.xml";

    @Rule
    public RuleChain copyFormChain = RuleChain
            .outerRule(GrantPermissionRule.grant(
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                    )
            )
            .around(new ResetStateRule())
            .around(new CopyFormRule(TEST_FORM, Collections.singletonList("doc.png")));

    @Rule
    public IntentsTestRule<FormEntryActivity> activityTestRule = FormLoadingUtils.getFormActivityTestRuleFor(TEST_FORM);

    @Test
    public void imageViewWithDefaultAnswer_shouldBeDisplayed() {
        new FormEntryPage("defaultImageAnswer", activityTestRule)
                .checkIfImageViewIsDisplayed()
                .swipeToNextQuestion()
                .checkIfImageViewIsDisplayed()
                .swipeToNextQuestion()
                .checkIfImageViewIsDisplayed()
                .swipeToNextQuestion()
                .checkIfImageViewIsNotDisplayed();
    }
}
