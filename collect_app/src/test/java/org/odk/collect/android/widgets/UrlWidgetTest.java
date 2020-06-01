package org.odk.collect.android.widgets;

import android.content.Context;
import android.net.Uri;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.widget.Button;
import android.widget.TextView;

import org.javarosa.core.model.data.StringData;
import org.javarosa.form.api.FormEntryPrompt;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.odk.collect.android.R;
import org.odk.collect.android.formentry.questions.QuestionDetails;
import org.odk.collect.android.utilities.CustomTabHelper;
import org.robolectric.RobolectricTestRunner;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.odk.collect.android.widgets.support.QuestionWidgetHelpers.promptWithAnswer;
import static org.odk.collect.android.widgets.support.QuestionWidgetHelpers.promptWithReadOnly;
import static org.odk.collect.android.widgets.support.QuestionWidgetHelpers.widgetTestActivity;

/**
 * @author James Knight
 */

@RunWith(RobolectricTestRunner.class)
public class UrlWidgetTest {

    private CustomTabHelper customTabHelper;
    private OnLongClickListener listener;

    @Before
    public void setUp() {
        customTabHelper = mock(CustomTabHelper.class);
        listener = new OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                return true;
            }
        };
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
        Button urlButton = widget.findViewById(R.id.url_button);
        assertThat(urlButton.getVisibility(), equalTo(View.GONE));
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
        assertThat(((TextView) widget.findViewById(R.id.url_answer_text)).getText().toString(), equalTo("blah"));
    }

    @Test
    public void whenPromptAnswerDoesNotHaveAnswer_displayEmptyString() {
        UrlWidget widget = createWidget(promptWithAnswer(null));
        assertThat(((TextView) widget.findViewById(R.id.url_answer_text)).getText().toString(), equalTo(""));
    }

    @Test
    public void clickingButtonWhenUrlIsEmpty_doesNotCallOpenUri() {
        UrlWidget widget = createWidget(promptWithAnswer(null));
        Button urlButton = widget.findViewById(R.id.url_button);
        urlButton.performClick();

        verify(customTabHelper, never()).bindCustomTabsService(null, null);
        verify(customTabHelper, never()).openUri(null, null);
    }

    @Test
    public void clickingButton_callsCorrectMethods() {
        UrlWidget widget = createWidget(promptWithAnswer(new StringData("blah")));
        Button urlButton = widget.findViewById(R.id.url_button);
        urlButton.performClick();

        verify(customTabHelper).bindCustomTabsService(widget.getContext(), null);
        verify(customTabHelper).openUri(widget.getContext(), Uri.parse("blah"));
    }

    @Test
    public void clickingButtonForLong_callsLongClickListener() {
        UrlWidget widget = createWidget(promptWithAnswer(new StringData("blah")));
        Button urlButton = widget.findViewById(R.id.url_button);
        urlButton.performLongClick();

        assertThat(listener.onLongClick(urlButton), equalTo(true));
    }

    private TestUrlWidget createWidget(FormEntryPrompt prompt) {
        return new TestUrlWidget(widgetTestActivity(), new QuestionDetails(prompt, "formAnalyticsID"), customTabHelper, listener);
    }

    private static class TestUrlWidget extends UrlWidget {

        public TestUrlWidget(Context context, QuestionDetails questionDetails, CustomTabHelper customTabHelper, OnLongClickListener listener) {
            super(context, questionDetails, customTabHelper);
            this.setOnLongClickListener(listener);
        }
    }
}
