package org.odk.collect.android.widgets;

import android.net.Uri;
import android.view.View;
import android.view.View.OnLongClickListener;

import androidx.browser.customtabs.CustomTabsServiceConnection;

import org.javarosa.core.model.data.StringData;
import org.javarosa.form.api.FormEntryPrompt;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.odk.collect.android.formentry.questions.QuestionDetails;
import org.odk.collect.android.support.TestScreenContextActivity;
import org.odk.collect.android.utilities.CustomTabHelper;
import org.robolectric.RobolectricTestRunner;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.odk.collect.android.widgets.support.QuestionWidgetHelpers.promptWithAnswer;
import static org.odk.collect.android.widgets.support.QuestionWidgetHelpers.promptWithReadOnly;
import static org.odk.collect.android.widgets.support.QuestionWidgetHelpers.widgetTestActivity;

/**
 * @author James Knight
 */

@RunWith(RobolectricTestRunner.class)
public class UrlWidgetTest {

    private TestScreenContextActivity spyActivity;
    private CustomTabHelper customTabHelper;
    private OnLongClickListener listener;

    @Before
    public void setUp() {
        spyActivity = spy(widgetTestActivity());
        customTabHelper = mock(CustomTabHelper.class);
        listener = mock(OnLongClickListener.class);
    }

    @Test
    public void getAnswer_whenPromptAnswerDoesNotHaveAnswer_returnsNull() {
        assertThat(createWidget(promptWithAnswer(null)).getAnswer(), nullValue());
    }

    @Test
    public void getAnswer_whenPromptHasAnswer_returnsAnswer() {
        UrlWidget widget = createWidget(promptWithAnswer(new StringData("blah")));
        assertThat(widget.getAnswer().getDisplayText(), equalTo("blah"));
    }

    @Test
    public void usingReadOnlyOption_makeAllClickableElementsDisabled() {
        UrlWidget widget = createWidget(promptWithReadOnly());
        assertThat(widget.getBinding().urlButton.getVisibility(), equalTo(View.GONE));
    }

    @Test
    public void clearAnswer_doesNotClearWidgetAnswer() {
        UrlWidget widget = createWidget(promptWithAnswer(new StringData("blah")));
        widget.clearAnswer();
        assertThat(widget.getAnswer().getDisplayText(), equalTo("blah"));
    }

    @Test
    public void whenPromptHasAnswer_displaysAnswer() {
        UrlWidget widget = createWidget(promptWithAnswer(new StringData("blah")));
        assertThat(widget.getBinding().urlAnswerText.getText().toString(), equalTo("blah"));
    }

    @Test
    public void whenPromptAnswerDoesNotHaveAnswer_displayEmptyString() {
        UrlWidget widget = createWidget(promptWithAnswer(null));
        assertThat(widget.getBinding().urlAnswerText.getText().toString(), equalTo(""));
    }

    @Test
    public void clickingButtonWhenUrlIsEmpty_doesNotCallOpenUri() {
        UrlWidget widget = createWidget(promptWithAnswer(null));
        widget.getBinding().urlButton.performClick();

        verify(customTabHelper, never()).bindCustomTabsService(null, null);
        verify(customTabHelper, never()).openUri(null, null);
    }

    @Test
    public void clickingButtonWhenUrlIsNotEmpty_callsOpenUri() {
        UrlWidget widget = createWidget(promptWithAnswer(new StringData("blah")));
        widget.getBinding().urlButton.performClick();

        verify(customTabHelper).bindCustomTabsService(widget.getContext(), null);
        verify(customTabHelper).openUri(widget.getContext(), Uri.parse("blah"));
    }

    @Test
    public void clickingButtonForLong_callsLongClickListener() {
        UrlWidget widget = createWidget(promptWithAnswer(null));
        widget.setOnLongClickListener(listener);
        widget.getBinding().urlButton.performLongClick();

        verify(listener).onLongClick(widget.getBinding().urlButton);
    }

    @Test
    public void detachingFromWindow_doesNotCallOnServiceDisconnected_whenServiceConnectionIsNull() {
        when(customTabHelper.getServiceConnection()).thenReturn(null);

        UrlWidget widget = createWidget(promptWithAnswer(null));
        widget.onDetachedFromWindow();
        verify(spyActivity, never()).unbindService(null);
    }

    @Test
    public void detachingFromWindow_disconnectsService_whenServiceConnectionIsNotNull() {
        CustomTabsServiceConnection serviceConnection = mock(CustomTabsServiceConnection.class);
        when(customTabHelper.getServiceConnection()).thenReturn(serviceConnection);

        UrlWidget widget = createWidget(promptWithAnswer(null));
        widget.onDetachedFromWindow();
        verify(spyActivity).unbindService(serviceConnection);
    }

    private UrlWidget createWidget(FormEntryPrompt prompt) {
        return new UrlWidget(spyActivity, new QuestionDetails(prompt, "formAnalyticsID"), customTabHelper);
    }
}
