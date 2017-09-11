package org.odk.collect.android.widgets.base;

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
import org.odk.collect.android.widgets.IQuestionWidget;
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

    private final Class<W> clazz;
    private W widget = null;

    @Mock
    public FormEntryPrompt formEntryPrompt;

    @Mock
    public FormIndex formIndex;

    @Mock
    public IFormElement formElement;

    @Mock
    public FormController formController;

    // Needs to be public for JUnit:
    @SuppressWarnings("WeakerAccess")
    public WidgetTest(Class<W> clazz) {
        this.clazz = clazz;
    }

    @NonNull
    public abstract W createWidget();

    @NonNull
    public abstract A getNextAnswer();

    @NonNull
    public abstract A getInitialAnswer();

    public W getWidget() {
        if (widget == null) {
            widget = spy(createWidget());
        }

        return widget;
    }

    @Before
    @OverridingMethodsMustInvokeSuper
    public void setUp() throws Exception {
        when(formEntryPrompt.getIndex()).thenReturn(formIndex);
        when(formEntryPrompt.getFormElement()).thenReturn(formElement);

        Collect.getInstance().setFormController(formController);

        widget = null;
    }

    @Test
    public final void getAnswerShouldReturnNullIfPromptDoesNotHaveExistingAnswer() {
        when(formEntryPrompt.getAnswerValue()).thenReturn(null);

        widget = getWidget();
        assertNull(widget.getAnswer());
    }

    @Test
    public final void getAnswerShouldReturnExistingAnswerIfPromptHasExistingAnswer() {
        A answer = getNextAnswer();
        when(formEntryPrompt.getAnswerValue()).thenReturn(answer);

        if (answer instanceof StringData) {
            when(formEntryPrompt.getAnswerText()).thenReturn((String) answer.getValue());
        }

        widget = getWidget();
        assertEquals(widget.getAnswer().getValue(), answer.getValue());
    }

    @Test
    public final void callingClearShouldRemoveTheExistingAnswer() {
        A answer = getNextAnswer();
        when(formEntryPrompt.getAnswerValue()).thenReturn(answer);

        if (clazz.isAssignableFrom(BinaryNameWidgetTest.class)) {
            when(formEntryPrompt.getAnswerText()).thenReturn((String) answer.getValue());
        }

        widget = getWidget();
        widget.clearAnswer();

        assertNull(widget.getAnswer());
    }
}