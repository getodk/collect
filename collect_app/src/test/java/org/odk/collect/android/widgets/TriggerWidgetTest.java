package org.odk.collect.android.widgets;

import android.support.annotation.NonNull;
import android.widget.CheckBox;

import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.core.model.data.StringData;
import org.junit.Test;
import org.odk.collect.android.widgets.base.QuestionWidgetTest;
import org.robolectric.RuntimeEnvironment;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * @author James Knight
 */

public class TriggerWidgetTest extends QuestionWidgetTest<TriggerWidget, StringData> {
    @NonNull
    @Override
    public TriggerWidget createWidget() {
        return new TriggerWidget(RuntimeEnvironment.application, formEntryPrompt);
    }

    @NonNull
    @Override
    public StringData getNextAnswer() {
        return new StringData(TriggerWidget.OK_TEXT);
    }

    @Override
    public void getAnswerShouldReturnExistingAnswerIfPromptHasExistingAnswer() {
        super.getAnswerShouldReturnExistingAnswerIfPromptHasExistingAnswer();
        assertTrue(getWidget().getTriggerButton().isChecked());
    }

    @Test
    public void checkingTheTriggerBoxShouldSetTheAnswer() {
        TriggerWidget widget = getWidget();
        assertNull(widget.getAnswer());

        CheckBox triggerButton = widget.getTriggerButton();
        assertFalse(triggerButton.isChecked());

        triggerButton.setChecked(true);
        triggerButton.callOnClick();

        IAnswerData answer = widget.getAnswer();
        assertEquals(answer.getDisplayText(), TriggerWidget.OK_TEXT);
    }
}
