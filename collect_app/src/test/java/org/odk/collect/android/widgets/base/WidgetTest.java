package org.odk.collect.android.widgets.base;


import org.javarosa.core.model.IFormElement;
import org.javarosa.form.api.FormEntryPrompt;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.odk.collect.android.BuildConfig;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import javax.annotation.OverridingMethodsMustInvokeSuper;

import static org.mockito.Mockito.when;

@Config(constants = BuildConfig.class)
@RunWith(RobolectricTestRunner.class)
public abstract class WidgetTest {

    @Rule
    public MockitoRule rule = MockitoJUnit.rule();

    @Mock
    public FormEntryPrompt formEntryPrompt;

    @Mock
    public IFormElement formElement;

    @Before
    @OverridingMethodsMustInvokeSuper
    public void setUp() throws Exception {
        when(formEntryPrompt.getFormElement()).thenReturn(formElement);
    }

    @Test
    public abstract void getAnswerShouldReturnNullIfPromptDoesNotHaveExistingAnswer();

    @Test
    public abstract void getAnswerShouldReturnExistingAnswerIfPromptHasExistingAnswer();

    @Test
    public abstract void callingClearShouldRemoveTheExistingAnswer();
}
