package org.odk.collect.android.feature.formentry;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.odk.collect.android.R;
import org.odk.collect.android.support.rules.BlankFormTestRule;
import org.odk.collect.android.support.rules.TestRuleChain;

public class ContextMenuTest {
    private static final String STRING_WIDGETS_TEST_FORM = "string_widgets_in_field_list.xml";

    public BlankFormTestRule activityTestRule = new BlankFormTestRule(STRING_WIDGETS_TEST_FORM, "fl");

    @Rule
    public RuleChain copyFormChain = TestRuleChain.chain()
            .around(activityTestRule);

    @Test
    public void whenRemoveStringAnswer_ShouldAppropriateQuestionBeCleared() {
        activityTestRule.fillNewForm()
                .answerQuestion(0, "TestString")
                .answerQuestion(1, "1234")
                .assertText("TestString")
                .assertText("1234")
                .longPressOnQuestion("Question1")
                .removeResponse()
                .assertTextDoesNotExist("TestString")
                .assertText("1234")
                .answerQuestion(0, "TestString")
                .assertText("TestString")
                .longPressOnQuestion("Question2")
                .removeResponse()
                .assertTextDoesNotExist("1234")
                .assertText("TestString");
    }

    @Test
    public void whenLongPressedOnEditText_ShouldNotRemoveAnswerOptionAppear() {
        activityTestRule.fillNewForm()
                .assertOnPage()
                .longPressOnQuestion(R.id.answer_container, 0)
                .assertTextDoesNotExist(org.odk.collect.strings.R.string.clear_answer);
    }
}
