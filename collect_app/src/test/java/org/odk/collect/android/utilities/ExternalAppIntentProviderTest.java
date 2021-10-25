package org.odk.collect.android.utilities;

import android.content.Intent;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.javarosa.core.model.FormIndex;
import org.javarosa.form.api.FormEntryPrompt;
import org.javarosa.xpath.parser.XPathSyntaxException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.odk.collect.android.exception.ExternalParamsException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.nullValue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(AndroidJUnit4.class)
public class ExternalAppIntentProviderTest {
    private FormEntryPrompt formEntryPrompt;
    private ExternalAppIntentProvider externalAppIntentProvider;

    @Before
    public void setup() {
        formEntryPrompt = mock(FormEntryPrompt.class);
        externalAppIntentProvider = new ExternalAppIntentProvider();

        when(formEntryPrompt.getIndex()).thenReturn(mock(FormIndex.class));
    }

    @Test
    public void intentAction_shouldBeSetProperly() throws ExternalParamsException, XPathSyntaxException {
        when(formEntryPrompt.getAppearanceHint()).thenReturn("ex:com.example.collectanswersprovider()");

        Intent resultIntent = externalAppIntentProvider.getIntentToRunExternalApp(formEntryPrompt);
        assertThat(resultIntent.getAction(), is("com.example.collectanswersprovider"));
    }

    @Test
    public void whenNoParamsSpecified_shouldIntentHaveNoExtras() throws ExternalParamsException, XPathSyntaxException {
        when(formEntryPrompt.getAppearanceHint()).thenReturn("ex:com.example.collectanswersprovider()");

        Intent resultIntent = externalAppIntentProvider.getIntentToRunExternalApp(formEntryPrompt);
        assertThat(resultIntent.getExtras(), nullValue());
    }

    @Test
    public void whenParamsSpecified_shouldIntentHaveExtras() throws ExternalParamsException, XPathSyntaxException {
        when(formEntryPrompt.getAppearanceHint()).thenReturn("ex:com.example.collectanswersprovider(param1='value1', param2='value2')");

        Intent resultIntent = externalAppIntentProvider.getIntentToRunExternalApp(formEntryPrompt);
        assertThat(resultIntent.getExtras().keySet().size(), is(2));
        assertThat(resultIntent.getExtras().getString("param1"), is("value1"));
        assertThat(resultIntent.getExtras().getString("param2"), is("value2"));
    }

    @Test
    public void whenParamsContainUri_shouldThatUriBeAddedAsIntentData() throws ExternalParamsException, XPathSyntaxException {
        when(formEntryPrompt.getAppearanceHint()).thenReturn("ex:com.example.collectanswersprovider(param1='value1', uri_data='file:///tmp/android.txt')");

        Intent resultIntent = externalAppIntentProvider.getIntentToRunExternalApp(formEntryPrompt);
        assertThat(resultIntent.getData().toString(), is("file:///tmp/android.txt"));
        assertThat(resultIntent.getExtras().keySet().size(), is(1));
        assertThat(resultIntent.getExtras().getString("param1"), is("value1"));
    }
}