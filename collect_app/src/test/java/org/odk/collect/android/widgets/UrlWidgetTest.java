package org.odk.collect.android.widgets;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.odk.collect.android.widgets.support.QuestionWidgetHelpers.promptWithAnswer;
import static org.odk.collect.android.widgets.support.QuestionWidgetHelpers.widgetDependencies;
import static org.odk.collect.android.widgets.support.QuestionWidgetHelpers.widgetTestActivity;

import android.app.Activity;
import android.net.Uri;
import android.view.View.OnLongClickListener;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.javarosa.core.model.data.StringData;
import org.javarosa.form.api.FormEntryPrompt;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.odk.collect.android.formentry.questions.QuestionDetails;
import org.odk.collect.android.support.WidgetTestActivity;
import org.odk.collect.webpage.WebPageService;
import org.robolectric.shadows.ShadowToast;

/**
 * @author James Knight
 */

@RunWith(AndroidJUnit4.class)
public class UrlWidgetTest {
    private WidgetTestActivity spyActivity;
    private WebPageService webPageService;
    private OnLongClickListener listener;

    @Before
    public void setUp() {
        spyActivity = spy(widgetTestActivity());
        webPageService = mock(WebPageService.class);
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
    public void clearAnswer_doesNotClearWidgetAnswer() {
        UrlWidget widget = createWidget(promptWithAnswer(new StringData("blah")));
        widget.clearAnswer();
        assertThat(widget.getAnswer().getDisplayText(), equalTo("blah"));
    }

    @Test
    public void clearAnswer_showsToastThatTheUrlIsReadOnly() {
        UrlWidget widget = createWidget(promptWithAnswer(new StringData("blah")));
        widget.clearAnswer();
        assertThat(ShadowToast.getTextOfLatestToast(), equalTo("URL is readonly"));
    }

    @Test
    public void clickingButton_whenUrlIsEmpty_doesNotOpensUri() {
        UrlWidget widget = createWidget(promptWithAnswer(null));
        widget.binding.urlButton.performClick();

        verify(webPageService, never()).openWebPage(null, null);
    }

    @Test
    public void clickingButton_whenUrlIsEmpty_showsToastMessage() {
        UrlWidget widget = createWidget(promptWithAnswer(null));
        widget.binding.urlButton.performClick();

        verify(webPageService, never()).openWebPage(null, null);
        assertThat(ShadowToast.getTextOfLatestToast(), equalTo("No URL set"));
    }

    @Test
    public void clickingButton_whenUrlIsNotEmpty_opensUriAndBindsCustomTabService() {
        UrlWidget widget = createWidget(promptWithAnswer(new StringData("blah")));
        widget.binding.urlButton.performClick();

        verify(webPageService).openWebPage((Activity) widget.getContext(), Uri.parse("blah"));
    }

    @Test
    public void clickingButtonForLong_callsLongClickListener() {
        UrlWidget widget = createWidget(promptWithAnswer(null));
        widget.setOnLongClickListener(listener);
        widget.binding.urlButton.performLongClick();
        verify(listener).onLongClick(widget.binding.urlButton);
    }

    private UrlWidget createWidget(FormEntryPrompt prompt) {
        return new UrlWidget(spyActivity, new QuestionDetails(prompt), webPageService, widgetDependencies());
    }
}
