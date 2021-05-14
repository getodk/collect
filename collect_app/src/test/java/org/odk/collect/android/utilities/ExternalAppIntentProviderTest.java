package org.odk.collect.android.utilities;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.javarosa.core.model.FormIndex;
import org.javarosa.form.api.FormEntryPrompt;
import org.javarosa.xpath.parser.XPathSyntaxException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.odk.collect.android.R;
import org.odk.collect.android.exception.ExternalParamsException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.nullValue;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(AndroidJUnit4.class)
public class ExternalAppIntentProviderTest {
    private Context context;
    private ActivityAvailability activityAvailability;
    private FormEntryPrompt formEntryPrompt;
    private PackageManager packageManager;
    private ExternalAppIntentProvider externalAppIntentProvider;

    @Before
    public void setup() {
        context = mock(Context.class);
        activityAvailability = mock(ActivityAvailability.class);
        formEntryPrompt = mock(FormEntryPrompt.class);
        packageManager = mock(PackageManager.class);
        externalAppIntentProvider = new ExternalAppIntentProvider();

        when(context.getString(R.string.no_app)).thenReturn("The requested application is missing. Please manually enter the reading.");
        when(formEntryPrompt.getIndex()).thenReturn(mock(FormIndex.class));
    }

    @Test
    public void whenExternalActivityNotAvailable_shouldExceptionBeThrown() {
        when(formEntryPrompt.getAppearanceHint()).thenReturn("ex:com.example.collectanswersprovider()");

        assertThrows(RuntimeException.class, () -> externalAppIntentProvider.getIntentToRunExternalApp(context, formEntryPrompt, activityAvailability, packageManager));
    }

    @Test
    public void whenNoCustomErrorMessageSpecified_shouldDefaultOneBeReturned() {
        when(formEntryPrompt.getAppearanceHint()).thenReturn("ex:com.example.collectanswersprovider()");

        Exception exception = assertThrows(RuntimeException.class, () -> externalAppIntentProvider.getIntentToRunExternalApp(context, formEntryPrompt, activityAvailability, packageManager));
        assertThat(exception.getMessage(), is("The requested application is missing. Please manually enter the reading."));
    }

    @Test
    public void whenCustomErrorMessageSpecified_shouldThatMessageReturned() {
        when(formEntryPrompt.getAppearanceHint()).thenReturn("ex:com.example.collectanswersprovider()");
        when(formEntryPrompt.getSpecialFormQuestionText("noAppErrorString")).thenReturn("Custom error message");

        Exception exception = assertThrows(RuntimeException.class, () -> externalAppIntentProvider.getIntentToRunExternalApp(context, formEntryPrompt, activityAvailability, packageManager));
        assertThat(exception.getMessage(), is("Custom error message"));
    }

    @Test
    public void intentAction_shouldBeSetProperly() throws ExternalParamsException, XPathSyntaxException {
        when(activityAvailability.isActivityAvailable(any())).thenReturn(true);
        when(formEntryPrompt.getAppearanceHint()).thenReturn("ex:com.example.collectanswersprovider()");

        Intent resultIntent = externalAppIntentProvider.getIntentToRunExternalApp(context, formEntryPrompt, activityAvailability, packageManager);
        assertThat(resultIntent.getAction(), is("com.example.collectanswersprovider"));
    }

    @Test
    public void whenNoParamsSpecified_shouldIntentHaveNoExtras() throws ExternalParamsException, XPathSyntaxException {
        when(activityAvailability.isActivityAvailable(any())).thenReturn(true);
        when(formEntryPrompt.getAppearanceHint()).thenReturn("ex:com.example.collectanswersprovider()");

        Intent resultIntent = externalAppIntentProvider.getIntentToRunExternalApp(context, formEntryPrompt, activityAvailability, packageManager);
        assertThat(resultIntent.getExtras(), nullValue());
    }

    @Test
    public void whenParamsSpecified_shouldIntentHaveExtras() throws ExternalParamsException, XPathSyntaxException {
        when(activityAvailability.isActivityAvailable(any())).thenReturn(true);
        when(formEntryPrompt.getAppearanceHint()).thenReturn("ex:com.example.collectanswersprovider(param1='value1', param2='value2')");

        Intent resultIntent = externalAppIntentProvider.getIntentToRunExternalApp(context, formEntryPrompt, activityAvailability, packageManager);
        assertThat(resultIntent.getExtras().keySet().size(), is(2));
        assertThat(resultIntent.getExtras().getString("param1"), is("value1"));
        assertThat(resultIntent.getExtras().getString("param2"), is("value2"));
    }

    @Test
    public void whenParamsContainUri_shouldThatUriBeAddedAsIntentData() throws ExternalParamsException, XPathSyntaxException {
        when(activityAvailability.isActivityAvailable(any())).thenReturn(true);
        when(formEntryPrompt.getAppearanceHint()).thenReturn("ex:com.example.collectanswersprovider(param1='value1', uri_data='file:///tmp/android.txt')");

        Intent resultIntent = externalAppIntentProvider.getIntentToRunExternalApp(context, formEntryPrompt, activityAvailability, packageManager);
        assertThat(resultIntent.getData().toString(), is("file:///tmp/android.txt"));
        assertThat(resultIntent.getExtras().keySet().size(), is(1));
        assertThat(resultIntent.getExtras().getString("param1"), is("value1"));
    }

    @Test
    public void whenSpecifiedIntentCanNotBeLaunched_shouldTryToLaunchTheMainExternalAppActivity() throws ExternalParamsException, XPathSyntaxException {
        when(formEntryPrompt.getAppearanceHint()).thenReturn("ex:com.example.collectanswersprovider()");
        Intent intent = mock(Intent.class);
        when(packageManager.getLaunchIntentForPackage("com.example.collectanswersprovider")).thenReturn(intent);
        when(activityAvailability.isActivityAvailable(intent)).thenReturn(true);

        Intent resultIntent = externalAppIntentProvider.getIntentToRunExternalApp(context, formEntryPrompt, activityAvailability, packageManager);
        assertThat(resultIntent, is(intent));
        assertThat(resultIntent.getFlags(), is(0));

    }
}