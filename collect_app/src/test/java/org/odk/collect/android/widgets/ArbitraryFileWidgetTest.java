package org.odk.collect.android.widgets;

import android.content.Intent;
import android.net.Uri;
import android.view.View;

import org.javarosa.core.model.FormIndex;
import org.javarosa.core.model.data.StringData;
import org.javarosa.form.api.FormEntryPrompt;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.odk.collect.android.BuildConfig;
import org.odk.collect.android.R;
import org.odk.collect.android.formentry.questions.QuestionDetails;
import org.odk.collect.android.listeners.WidgetValueChangedListener;
import org.odk.collect.android.support.TestScreenContextActivity;
import org.odk.collect.android.utilities.ActivityAvailability;
import org.odk.collect.android.utilities.ApplicationConstants;
import org.odk.collect.android.utilities.ContentUriProvider;
import org.odk.collect.android.utilities.QuestionMediaManager;
import org.odk.collect.android.widgets.support.FakeWaitingForDataRegistry;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.shadows.ShadowActivity;
import org.robolectric.shadows.ShadowToast;

import java.io.File;
import java.io.IOException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.odk.collect.android.widgets.support.QuestionWidgetHelpers.mockValueChangedListener;
import static org.odk.collect.android.widgets.support.QuestionWidgetHelpers.promptWithAnswer;
import static org.odk.collect.android.widgets.support.QuestionWidgetHelpers.promptWithReadOnly;
import static org.odk.collect.android.widgets.support.QuestionWidgetHelpers.widgetTestActivity;
import static org.robolectric.Shadows.shadowOf;

@RunWith(RobolectricTestRunner.class)
public class ArbitraryFileWidgetTest {

    private TestScreenContextActivity widgetActivity;
    private ShadowActivity shadowActivity;
    private FakeWaitingForDataRegistry waitingForDataRegistry;
    private QuestionMediaManager mockedQuestionMediaManager;
    private FormIndex formIndex;
    private ActivityAvailability activityAvailability;
    private ContentUriProvider contentUriProvider;

    @Before
    public void setUp() {
        widgetActivity = widgetTestActivity();
        shadowActivity = shadowOf(widgetActivity);

        mockedQuestionMediaManager = mock(QuestionMediaManager.class);
        activityAvailability = mock(ActivityAvailability.class);
        contentUriProvider = mock(ContentUriProvider.class);
        formIndex = mock(FormIndex.class);

        waitingForDataRegistry = new FakeWaitingForDataRegistry();

        when(activityAvailability.isActivityAvailable(ArgumentMatchers.any())).thenReturn(true);
        when(formIndex.toString()).thenReturn("questionIndex");
    }

    @Test
    public void usingReadOnlyOption_doesNotDisplayChooseFileButton() {
        assertThat(createWidget(promptWithReadOnly()).binding.chooseFileButton.getVisibility(), is(View.GONE));
    }

    @Test
    public void whenPromptDoesNotHaveAnswer_answerLayoutIsNotDisplayed() {
        assertThat(createWidget(promptWithAnswer(null)).binding.answerLayout.getVisibility(), is(View.GONE));
    }

    @Test
    public void getAnswer_whenPromptDoesNotHaveAnswer_returnsNull() {
        assertNull(createWidget(promptWithAnswer(null)).getAnswer());
    }

    @Test
    public void getAnswer_whenPromptHasAnswer_returnsAnswer() {
        ArbitraryFileWidget widget = createWidget(promptWithAnswer(new StringData("blah.txt")));
        assertThat(widget.getAnswer().getDisplayText(), is("blah.txt"));
    }

    @Test
    public void whenPromptDoesNotHaveAnswer_answerTextViewShowsEmptyString() {
        ArbitraryFileWidget widget = createWidget(promptWithAnswer(null));
        assertThat(widget.binding.answerTextView.getText(), is(""));
    }

    @Test
    public void whenPromptHasAnswer_answerTextViewShowsFileName() {
        ArbitraryFileWidget widget = createWidget(promptWithAnswer(new StringData("blah.txt")));
        assertThat(widget.binding.answerTextView.getText(), is("blah.txt"));
    }

    @Test
    public void clearAnswer_hidesAnswerLayout() {
        ArbitraryFileWidget widget = createWidget(promptWithAnswer(new StringData("blah.txt")));
        widget.clearAnswer();
        assertThat(widget.binding.answerLayout.getVisibility(), is(View.GONE));
    }

    @Test
    public void clearAnswer_removesAnswer() {
        ArbitraryFileWidget widget = createWidget(promptWithAnswer(new StringData("blah.txt")));
        widget.clearAnswer();
        assertNull(widget.getAnswer());
    }

    @Test
    public void clearAnswer_callsMarkOriginalFileOrDelete() {
        FormEntryPrompt prompt = promptWithAnswer(new StringData("blah.txt"));
        when(prompt.getIndex()).thenReturn(formIndex);

        ArbitraryFileWidget widget = createWidget(prompt);
        widget.clearAnswer();
        verify(mockedQuestionMediaManager).markOriginalFileOrDelete("questionIndex",
                "null" + File.separator + "blah.txt");
    }

    @Test
    public void clearAnswer_callsValueChangeListeners() {
        ArbitraryFileWidget widget = createWidget(promptWithAnswer(new StringData("blah.txt")));
        WidgetValueChangedListener valueChangedListener = mockValueChangedListener(widget);
        widget.clearAnswer();

        verify(valueChangedListener).widgetValueChanged(widget);
    }

    @Test
    public void setData_whenFileDoesNotExist_doesNotUpdateWidgetAnswer() {
        ArbitraryFileWidget widget = createWidget(promptWithAnswer(new StringData("blah.txt")));
        widget.setBinaryData(new File("newFilePath"));

        assertThat(widget.getAnswer().getDisplayText(), is("blah.txt"));
        assertThat(widget.binding.answerTextView.getText(), is("blah.txt"));
    }

    @Test
    public void setData_whenFileDoesNotExist_doesNotCallValueChangeListener() {
        ArbitraryFileWidget widget = createWidget(promptWithAnswer(new StringData("blah.txt")));
        WidgetValueChangedListener valueChangedListener = mockValueChangedListener(widget);
        widget.setBinaryData(new File("newFilePath"));

        verify(valueChangedListener, never()).widgetValueChanged(widget);
    }

    @Test
    public void setData_whenFileExists_updatesWidgetAnswer() throws IOException {
        File tempFile = File.createTempFile("newFile", "txt");
        tempFile.deleteOnExit();

        ArbitraryFileWidget widget = createWidget(promptWithAnswer(null));
        widget.setBinaryData(tempFile);

        assertThat(widget.getAnswer().getDisplayText(), is(tempFile.getName()));
        assertThat(widget.binding.answerTextView.getText(), is(tempFile.getName()));
    }

    @Test
    public void setData_whenFileExists_callsValueChangeListener() throws IOException {
        File tempFile = File.createTempFile("newFile", "txt");
        tempFile.deleteOnExit();

        ArbitraryFileWidget widget = createWidget(promptWithAnswer(null));
        WidgetValueChangedListener valueChangedListener = mockValueChangedListener(widget);

        widget.setBinaryData(tempFile);
        verify(valueChangedListener).widgetValueChanged(widget);
    }

    @Test
    public void clickingButtonAndAnswerForLong_callsOnLongClickListeners() {
        View.OnLongClickListener listener = mock(View.OnLongClickListener.class);
        ArbitraryFileWidget widget = createWidget(promptWithAnswer(null));
        widget.setOnLongClickListener(listener);

        widget.binding.chooseFileButton.performLongClick();
        widget.binding.answerLayout.performLongClick();

        verify(listener).onLongClick(widget.binding.chooseFileButton);
        verify(listener).onLongClick(widget.binding.answerLayout);
    }

    @Test
    public void clickingChooseFileButton_startsOpenDocumentIntent_andSetsWidgetWaitingForData() {
        FormEntryPrompt prompt = promptWithAnswer(null);
        when(prompt.getIndex()).thenReturn(formIndex);

        ArbitraryFileWidget widget = createWidget(prompt);
        widget.binding.chooseFileButton.performClick();

        Intent startedActivity = shadowActivity.getNextStartedActivity();

        assertThat(startedActivity.getAction(), equalTo(Intent.ACTION_OPEN_DOCUMENT));
        assertThat(startedActivity.getCategories().contains(Intent.CATEGORY_OPENABLE), is(true));
        assertThat(startedActivity.getType(), equalTo("*/*"));

        ShadowActivity.IntentForResult intentForResult = shadowActivity.getNextStartedActivityForResult();
        assertThat(intentForResult.requestCode, equalTo(ApplicationConstants.RequestCodes.ARBITRARY_FILE_CHOOSER));

        assertThat(waitingForDataRegistry.waiting.contains(formIndex), equalTo(true));
    }

    @Test
    public void clickingAnswer_whenActivityIsNotAvailable_doesNotStartAnyIntent() {
        when(activityAvailability.isActivityAvailable(any())).thenReturn(false);
        ArbitraryFileWidget widget = createWidget(promptWithAnswer(new StringData("blah.txt")));
        widget.binding.answerLayout.performClick();

        assertNull(shadowActivity.getNextStartedActivity());
        assertThat(ShadowToast.getTextOfLatestToast(), is(widgetActivity.getString(R.string.activity_not_found,
                widgetActivity.getString(R.string.open_file))));
    }

    @Test
    public void clickingAnswer_whenActivityIsAvailable_startsViewIntent() {
        when(activityAvailability.isActivityAvailable(any())).thenReturn(true);
        ArbitraryFileWidget widget = createWidget(promptWithAnswer(new StringData("blah.txt")));

        when(contentUriProvider.getUriForFile(widgetActivity,
                BuildConfig.APPLICATION_ID + ".provider",
                new File("null" + File.separator + "blah.txt"))).thenReturn(Uri.parse("content://blah"));

        widget.binding.answerLayout.performClick();
        Intent startedActivity = shadowActivity.getNextStartedActivity();

        assertThat(startedActivity.getAction(), equalTo(Intent.ACTION_VIEW));
        assertThat(startedActivity.getData(), is(Uri.parse("content://blah")));
        assertThat(startedActivity.getFlags(), equalTo(Intent.FLAG_GRANT_READ_URI_PERMISSION));
    }

    public ArbitraryFileWidget createWidget(FormEntryPrompt prompt) {
        return new ArbitraryFileWidget(widgetActivity, new QuestionDetails(prompt, "formAnalyticsID"),
                mockedQuestionMediaManager, waitingForDataRegistry, activityAvailability, contentUriProvider);
    }
}
