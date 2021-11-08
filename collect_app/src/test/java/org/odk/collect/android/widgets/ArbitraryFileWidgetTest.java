package org.odk.collect.android.widgets;

import android.view.View;

import androidx.annotation.NonNull;

import org.javarosa.core.model.data.StringData;
import org.junit.Test;
import org.mockito.Mock;
import org.odk.collect.android.formentry.questions.QuestionDetails;
import org.odk.collect.android.utilities.ApplicationConstants;
import org.odk.collect.android.utilities.MediaUtils;
import org.odk.collect.android.widgets.base.FileWidgetTest;
import org.odk.collect.android.widgets.support.FakeQuestionMediaManager;
import org.odk.collect.android.widgets.support.FakeWaitingForDataRegistry;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.odk.collect.android.preferences.keys.ProjectKeys.KEY_FONT_SIZE;
import static org.odk.collect.android.utilities.QuestionFontSizeUtils.DEFAULT_FONT_SIZE;

public class ArbitraryFileWidgetTest extends FileWidgetTest<ArbitraryFileWidget> {
    @Mock
    MediaUtils mediaUtils;

    @Override
    public StringData getInitialAnswer() {
        return new StringData("document.pdf");
    }

    @NonNull
    @Override
    public StringData getNextAnswer() {
        return new StringData("document.xlsx");
    }

    @NonNull
    @Override
    public ArbitraryFileWidget createWidget() {
        return new ArbitraryFileWidget(activity, new QuestionDetails(formEntryPrompt, readOnlyOverride),
                mediaUtils, new FakeQuestionMediaManager(), new FakeWaitingForDataRegistry());
    }

    @Test
    public void whenFontSizeNotChanged_defaultFontSizeShouldBeUsed() {
        assertThat((int) getWidget().binding.arbitraryFileButton.getTextSize(), is(DEFAULT_FONT_SIZE - 1));
        assertThat((int) getWidget().binding.arbitraryFileAnswerText.getTextSize(), is(DEFAULT_FONT_SIZE - 1));
    }

    @Test
    public void whenFontSizeChanged_CustomFontSizeShouldBeUsed() {
        settingsProvider.getUnprotectedSettings().save(KEY_FONT_SIZE, "30");

        assertThat((int) getWidget().binding.arbitraryFileButton.getTextSize(), is(29));
        assertThat((int) getWidget().binding.arbitraryFileAnswerText.getTextSize(), is(29));
    }

    @Test
    public void whenThereIsNoAnswer_shouldAnswerTextBeHidden() {
        assertThat(getWidget().binding.arbitraryFileAnswerText.getVisibility(), is(View.GONE));
    }

    @Test
    public void whenThereIsAnswer_shouldAnswerTextBeDisplayed() {
        when(formEntryPrompt.getAnswerText()).thenReturn(getInitialAnswer().getDisplayText());

        ArbitraryFileWidget widget = getWidget();
        assertThat(widget.binding.arbitraryFileAnswerText.getVisibility(), is(View.VISIBLE));
        assertThat(widget.binding.arbitraryFileAnswerText.getText(), is(getInitialAnswer().getDisplayText()));
    }

    @Test
    public void whenClickingOnButton_shouldFilePickerBeCalled() {
        getWidget().binding.arbitraryFileButton.performClick();
        verify(mediaUtils).pickFile(activity, "*/*", ApplicationConstants.RequestCodes.ARBITRARY_FILE_CHOOSER);
    }

    @Test
    public void whenClickingOnAnswer_shouldFileViewerByCalled() {
        when(formEntryPrompt.getAnswerText()).thenReturn(getInitialAnswer().getDisplayText());

        ArbitraryFileWidget widget = getWidget();
        widget.binding.arbitraryFileAnswerText.performClick();
        verify(mediaUtils).openFile(activity, widget.answerFile, null);
    }

    @Test
    public void whenClearAnswerCall_shouldAnswerTextBeHidden() {
        when(formEntryPrompt.getAnswerText()).thenReturn(getInitialAnswer().getDisplayText());

        ArbitraryFileWidget widget = getWidget();
        widget.clearAnswer();
        assertThat(widget.binding.arbitraryFileAnswerText.getVisibility(), is(View.GONE));
    }

    @Test
    public void whenSetDataCalledWithUnsupportedType_shouldAnswerBeRemoved() {
        when(formEntryPrompt.getAnswerText()).thenReturn(getInitialAnswer().getDisplayText());

        ArbitraryFileWidget widget = getWidget();
        widget.setData(null);
        assertThat(widget.getAnswer(), is(nullValue()));
        assertThat(widget.binding.arbitraryFileAnswerText.getVisibility(), is(View.GONE));
    }

    @Test
    public void usingReadOnlyOptionShouldMakeAllClickableElementsDisabled() {
        when(formEntryPrompt.isReadOnly()).thenReturn(true);
        when(formEntryPrompt.getAnswerText()).thenReturn(getInitialAnswer().getDisplayText());

        ArbitraryFileWidget widget = getWidget();
        assertThat(widget.binding.arbitraryFileButton.getVisibility(), is(View.GONE));
        assertThat(widget.binding.arbitraryFileAnswerText.getVisibility(), is(View.VISIBLE));
        assertThat(widget.binding.arbitraryFileAnswerText.getText(), is(getInitialAnswer().getDisplayText()));
        assertThat(widget.binding.arbitraryFileAnswerText.hasOnClickListeners(), is(true));
    }

    @Test
    public void whenReadOnlyOverrideOptionIsUsed_shouldAllClickableElementsBeDisabled() {
        readOnlyOverride = true;
        when(formEntryPrompt.isReadOnly()).thenReturn(false);
        when(formEntryPrompt.getAnswerText()).thenReturn(getInitialAnswer().getDisplayText());

        ArbitraryFileWidget widget = getWidget();
        assertThat(widget.binding.arbitraryFileButton.getVisibility(), is(View.GONE));
        assertThat(widget.binding.arbitraryFileAnswerText.getVisibility(), is(View.VISIBLE));
        assertThat(widget.binding.arbitraryFileAnswerText.getText(), is(getInitialAnswer().getDisplayText()));
        assertThat(widget.binding.arbitraryFileAnswerText.hasOnClickListeners(), is(true));
    }
}
