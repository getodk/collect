package org.odk.collect.android.widgets;

import android.support.annotation.NonNull;

import org.javarosa.core.model.FormIndex;
import org.javarosa.core.model.IFormElement;
import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.core.model.data.StringData;
import org.javarosa.form.api.FormEntryPrompt;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.odk.collect.android.BuildConfig;
import org.odk.collect.android.application.Collect;
import org.odk.collect.android.logic.FormController;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import javax.annotation.OverridingMethodsMustInvokeSuper;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;


@Config(constants = BuildConfig.class)
@RunWith(RobolectricTestRunner.class)
public abstract class WidgetTest<W extends IQuestionWidget, A extends IAnswerData> {

    @Rule
    public MockitoRule rule = MockitoJUnit.rule();

    W widget = null;
    A answer = null;

    @Mock
    FormEntryPrompt formEntryPrompt;

    @Mock
    FormIndex formIndex;

    @Mock
    IFormElement formElement;

    @Mock
    FormController formController;

    @NonNull
    abstract W createWidget();

    @NonNull
    abstract A createAnswer();

    W createSpy() {
        return spy(createWidget());
    }

    @Before
    @OverridingMethodsMustInvokeSuper
    public void setUp() throws Exception {
        when(formEntryPrompt.getIndex()).thenReturn(formIndex);
        when(formEntryPrompt.getFormElement()).thenReturn(formElement);

        Collect.getInstance().setFormController(formController);

        widget = null;
        answer = null;
    }

    @Test
    public final void getAnswerShouldReturnNullIfPromptDoesntHaveExistingAnswer() {
        when(formEntryPrompt.getAnswerValue()).thenReturn(null);

        widget = createWidget();
        assertNull(widget.getAnswer());
    }

    @Test
    public final void getAnswerShouldReturnExistingAnswerIfPromptHasExistingAnswer() {

        answer = createAnswer();
        when(formEntryPrompt.getAnswerValue()).thenReturn(answer);

        if (answer instanceof StringData) {
            when(formEntryPrompt.getAnswerText()).thenReturn((String) answer.getValue());
        }

        widget = createWidget();
        assertEquals(widget.getAnswer().getValue(), answer.getValue());
    }

    @Test
    public final void callingClearShouldRemoveTheExistingAnswer() {
        IAnswerData answer = createAnswer();
        when(formEntryPrompt.getAnswerValue()).thenReturn(answer);

        if (answer instanceof StringData) {
            when(formEntryPrompt.getAnswerText()).thenReturn((String) answer.getValue());
        }

        widget = createWidget();
        widget.clearAnswer();

        assertNull(widget.getAnswer());
    }
}