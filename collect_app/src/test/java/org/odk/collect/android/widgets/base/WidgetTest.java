package org.odk.collect.android.widgets.base;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.javarosa.core.model.FormIndex;
import org.javarosa.core.model.IFormElement;
import org.javarosa.form.api.FormEntryPrompt;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import org.odk.collect.android.TestSettingsProvider;
import org.odk.collect.android.preferences.source.SettingsProvider;

import javax.annotation.OverridingMethodsMustInvokeSuper;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(AndroidJUnit4.class)
public abstract class WidgetTest {

    @Rule
    public MockitoRule rule = MockitoJUnit.rule();

    @Mock
    public FormEntryPrompt formEntryPrompt;

    @Mock
    public IFormElement formElement;

    public boolean readOnlyOverride;

    protected final SettingsProvider settingsProvider = TestSettingsProvider.getSettingsProvider();

    @Before
    @OverridingMethodsMustInvokeSuper
    public void setUp() throws Exception {
        settingsProvider.getUnprotectedSettings().clear();
        settingsProvider.getUnprotectedSettings().setDefaultForAllSettingsWithoutValues();
        settingsProvider.getProtectedSettings().clear();
        settingsProvider.getProtectedSettings().setDefaultForAllSettingsWithoutValues();

        when(formEntryPrompt.getIndex()).thenReturn(mock(FormIndex.class));
        when(formEntryPrompt.getIndex().toString()).thenReturn("0, 0");
        when(formEntryPrompt.getFormElement()).thenReturn(formElement);
    }

    @Test
    public abstract void getAnswerShouldReturnNullIfPromptDoesNotHaveExistingAnswer();

    @Test
    public abstract void getAnswerShouldReturnExistingAnswerIfPromptHasExistingAnswer();

    @Test
    public abstract void callingClearShouldRemoveTheExistingAnswer();

    @Test
    public abstract void callingClearShouldCallValueChangeListeners();

    @Test
    public abstract void usingReadOnlyOptionShouldMakeAllClickableElementsDisabled();
}
