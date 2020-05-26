package org.odk.collect.android.widgets.base;

import android.view.View;

import org.javarosa.core.model.data.IAnswerData;
import org.junit.Test;
import org.odk.collect.android.widgets.ExStringWidget;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;

/**
 * @author James Knight
 */

public abstract class GeneralExStringWidgetTest<W extends ExStringWidget, A extends IAnswerData> extends BinaryWidgetTest<W, A> {

    @Override
    public Object createBinaryData(A answerData) {
        return answerData.getDisplayText();
    }

    @Test
    public void usingReadOnlyOptionShouldMakeAllClickableElementsDisabled() {
        when(formEntryPrompt.isReadOnly()).thenReturn(true);

        assertThat(getSpyWidget().launchIntentButton.getVisibility(), is(View.GONE));
        assertThat(getSpyWidget().answerText.getVisibility(), is(View.VISIBLE));
        assertThat(getSpyWidget().answerText.isEnabled(), is(Boolean.FALSE));
    }
}
