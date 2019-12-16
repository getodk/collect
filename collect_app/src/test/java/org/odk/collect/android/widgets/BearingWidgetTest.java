package org.odk.collect.android.widgets;

import android.view.View;

import androidx.annotation.NonNull;

import net.bytebuddy.utility.RandomString;

import org.javarosa.core.model.data.StringData;
import org.junit.Before;
import org.junit.Test;
import org.odk.collect.android.formentry.questions.QuestionDetails;
import org.odk.collect.android.widgets.base.BinaryWidgetTest;

import static junit.framework.TestCase.assertEquals;
import static org.mockito.Mockito.when;

/**
 * @author James Knight
 */
public class BearingWidgetTest extends BinaryWidgetTest<BearingWidget, StringData> {

    private String barcodeData;

    @NonNull
    @Override
    public BearingWidget createWidget() {
        return new BearingWidget(activity, new QuestionDetails(formEntryPrompt, "formAnalyticsID"));
    }

    @Override
    public Object createBinaryData(StringData answerData) {
        return barcodeData;
    }

    @NonNull
    @Override
    public StringData getNextAnswer() {
        return new StringData(barcodeData);
    }

    @Override
    public StringData getInitialAnswer() {
        return new StringData(RandomString.make());
    }

    @Before
    public void setUp() throws Exception {
        super.setUp();
        barcodeData = RandomString.make();
    }

    @Test
    public void readOnlyTest() {
        when(formEntryPrompt.isReadOnly()).thenReturn(true);

        assertEquals(View.GONE, getWidget().getBearingButton.getVisibility());
        assertEquals(View.VISIBLE, getWidget().answer.getVisibility());
    }
}
