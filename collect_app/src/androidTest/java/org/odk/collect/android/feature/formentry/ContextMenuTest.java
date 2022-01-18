package org.odk.collect.android.feature.formentry;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.odk.collect.android.R;
import org.odk.collect.android.support.CollectTestRule;
import org.odk.collect.android.support.FormActivityTestRule;
import org.odk.collect.android.support.TestRuleChain;

public class ContextMenuTest {
    private static final String STRING_WIDGETS_TEST_FORM = "string_widgets_in_field_list.xml";

    private final CollectTestRule rule = new CollectTestRule(STRING_WIDGETS_TEST_FORM, true);

    @Rule
    public FormActivityTestRule activityTestRule = new FormActivityTestRule(STRING_WIDGETS_TEST_FORM, "fl");

    @Rule
    public RuleChain copyFormChain = TestRuleChain.chain()
            .around(rule);

    @Test
    public void whenRemoveStringAnswer_ShouldAppropriateQuestionBeCleared() {
        activityTestRule.startInFormEntry()
                .assertOnPage()
                .answerQuestion(0, "TestString")
                .answerQuestion(1, "1234")
                .assertText("TestString")
                .assertText("1234")
                .longPressOnView("Question1")
                .removeResponse()
                .assertTextDoesNotExist("TestString")
                .assertText("1234")
                .answerQuestion(0, "TestString")
                .assertText("TestString")
                .longPressOnView("Question2")
                .removeResponse()
                .assertTextDoesNotExist("1234")
                .assertText("TestString");
    }

    @Test
    public void whenLongPressedOnEditText_ShouldNotRemoveAnswerOptionAppear() {
        activityTestRule.startInFormEntry()
                .assertOnPage()
                .longPressOnView(R.id.answer_container, 0)
                .assertTextDoesNotExist(R.string.clear_answer);
    }
}
