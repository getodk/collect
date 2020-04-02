package org.odk.collect.android.formentry;

import android.Manifest;

import androidx.test.espresso.intent.rule.IntentsTestRule;
import androidx.test.rule.GrantPermissionRule;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.odk.collect.android.R;
import org.odk.collect.android.activities.FormEntryActivity;
import org.odk.collect.android.support.CopyFormRule;
import org.odk.collect.android.support.ResetStateRule;
import org.odk.collect.android.support.pages.FormEntryPage;
import org.odk.collect.android.test.FormLoadingUtils;

public class ContextMenuTest {
    private static final String STRING_WIDGETS_TEST_FORM = "string_widgets_in_field_list.xml";

    @Rule
    public IntentsTestRule<FormEntryActivity> activityTestRule = FormLoadingUtils.getFormActivityTestRuleFor(STRING_WIDGETS_TEST_FORM);

    @Rule
    public RuleChain copyFormChain = RuleChain
            .outerRule(GrantPermissionRule.grant(
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)
            )
            .around(new ResetStateRule())
            .around(new CopyFormRule(STRING_WIDGETS_TEST_FORM, true));

    @Test
    public void whenRemoveStringAnswer_ShouldAppropriateQuestionBeCleared() {
        new FormEntryPage("string_widgets", activityTestRule)
                .putTextOnIndex(0, "TestString")
                .putTextOnIndex(1, "1234")
                .assertText("TestString")
                .assertText("1234")
                .longPressOnView("Question1")
                .removeResponse()
                .checkIfTextDoesNotExist("TestString")
                .assertText("1234")
                .putTextOnIndex(0, "TestString")
                .assertText("TestString")
                .longPressOnView("Question2")
                .removeResponse()
                .checkIfTextDoesNotExist("1234")
                .assertText("TestString");
    }

    @Test
    public void whenLongPressedOnEditText_ShouldNotRemoveAnswerOptionAppear() {
        new FormEntryPage("string_widgets", activityTestRule)
                .longPressOnView(R.id.answer_container, 0)
                .checkIfTextDoesNotExist(R.string.clear_answer);
    }
}