package org.odk.collect.android.widgets;

import android.view.View;

import androidx.annotation.NonNull;

import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.core.model.data.StringData;
import org.junit.Test;
import org.odk.collect.android.formentry.questions.QuestionDetails;
import org.odk.collect.android.widgets.base.QuestionWidgetTest;

import static junit.framework.TestCase.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.when;

public class ExPrinterWidgetTest extends QuestionWidgetTest<ExPrinterWidget, IAnswerData> {

    @NonNull
    @Override
    public ExPrinterWidget createWidget() {
        return new ExPrinterWidget(activity, new QuestionDetails(formEntryPrompt, "formAnalyticsID"));
    }

    @NonNull
    @Override
    public IAnswerData getNextAnswer() {
        return new StringData("123456789<br>QRCODE<br>Text");
    }

    @Test
    @Override
    // ExPrintWidget is and exceptional widget that doesn't return any answer
    public void callingClearShouldRemoveTheExistingAnswer() {
    }

    @Test
    @Override
    // ExPrintWidget is and exceptional widget that doesn't return any answer
    public void getAnswerShouldReturnExistingAnswerIfPromptHasExistingAnswer() {
        IAnswerData newAnswer = getWidget().getAnswer();
        assertNull(newAnswer);
    }

    @Test
    public void usingReadOnlyOptionShouldMakeAllClickableElementsDisabled() {
        when(formEntryPrompt.isReadOnly()).thenReturn(true);

        assertEquals(View.GONE, getWidget().launchIntentButton.getVisibility());
    }
}
