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
import org.odk.collect.android.widgets.utilities.FileRequester;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.odk.collect.android.preferences.keys.ProjectKeys.KEY_FONT_SIZE;
import static org.odk.collect.android.utilities.QuestionFontSizeUtils.DEFAULT_FONT_SIZE;

public class ExArbitraryFileWidgetTest extends FileWidgetTest<ExArbitraryFileWidget> {
    @Mock
    MediaUtils mediaUtils;

    @Mock
    FileRequester fileRequester;

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
    public ExArbitraryFileWidget createWidget() {
        return new ExArbitraryFileWidget(activity, new QuestionDetails(formEntryPrompt, readOnlyOverride),
                mediaUtils, new FakeQuestionMediaManager(), new FakeWaitingForDataRegistry(), fileRequester);
    }

    @Test
    public void whenFontSizeNotChanged_defaultFontSizeShouldBeUsed() {
        assertThat((int) getWidget().binding.exArbitraryFileButton.getTextSize(), is(DEFAULT_FONT_SIZE - 1));
        assertThat((int) getWidget().binding.exArbitraryFileAnswerText.getTextSize(), is(DEFAULT_FONT_SIZE - 1));
    }

    @Test
    public void whenFontSizeChanged_CustomFontSizeShouldBeUsed() {
        settingsProvider.getUnprotectedSettings().save(KEY_FONT_SIZE, "30");

        assertThat((int) getWidget().binding.exArbitraryFileButton.getTextSize(), is(29));
        assertThat((int) getWidget().binding.exArbitraryFileAnswerText.getTextSize(), is(29));
    }

    @Test
    public void whenThereIsNoAnswer_shouldAnswerTextBeHidden() {
        assertThat(getWidget().binding.exArbitraryFileAnswerText.getVisibility(), is(View.GONE));
    }

    @Test
    public void whenThereIsAnswer_shouldAnswerTextBeDisplayed() {
        when(formEntryPrompt.getAnswerText()).thenReturn(getInitialAnswer().getDisplayText());

        ExArbitraryFileWidget widget = getWidget();
        assertThat(widget.binding.exArbitraryFileAnswerText.getVisibility(), is(View.VISIBLE));
        assertThat(widget.binding.exArbitraryFileAnswerText.getText(), is(getInitialAnswer().getDisplayText()));
    }

    @Test
    public void whenClickingOnButton_exWidgetIntentLauncherShouldBeStarted() {
        getWidget().binding.exArbitraryFileButton.performClick();
        verify(fileRequester).launch(activity, ApplicationConstants.RequestCodes.EX_ARBITRARY_FILE_CHOOSER, formEntryPrompt);
    }

    @Test
    public void whenClickingOnAnswer_shouldFileViewerByCalled() {
        when(formEntryPrompt.getAnswerText()).thenReturn(getInitialAnswer().getDisplayText());

        ExArbitraryFileWidget widget = getWidget();
        widget.binding.exArbitraryFileAnswerText.performClick();
        verify(mediaUtils).openFile(activity, widget.answerFile, null);
    }

    @Test
    public void whenClearAnswerCall_shouldAnswerTextBeHidden() {
        when(formEntryPrompt.getAnswerText()).thenReturn(getInitialAnswer().getDisplayText());

        ExArbitraryFileWidget widget = getWidget();
        widget.clearAnswer();
        assertThat(widget.binding.exArbitraryFileAnswerText.getVisibility(), is(View.GONE));
    }

    @Test
    public void whenSetDataCalledWithUnsupportedType_shouldAnswerBeRemoved() {
        when(formEntryPrompt.getAnswerText()).thenReturn(getInitialAnswer().getDisplayText());

        ExArbitraryFileWidget widget = getWidget();
        widget.setData(null);
        assertThat(widget.getAnswer(), is(nullValue()));
        assertThat(widget.binding.exArbitraryFileAnswerText.getVisibility(), is(View.GONE));
    }

    @Test
    public void usingReadOnlyOptionShouldMakeAllClickableElementsDisabled() {
        when(formEntryPrompt.isReadOnly()).thenReturn(true);
        when(formEntryPrompt.getAnswerText()).thenReturn(getInitialAnswer().getDisplayText());

        ExArbitraryFileWidget widget = getWidget();
        assertThat(widget.binding.exArbitraryFileButton.getVisibility(), is(View.GONE));
        assertThat(widget.binding.exArbitraryFileAnswerText.getVisibility(), is(View.VISIBLE));
        assertThat(widget.binding.exArbitraryFileAnswerText.getText(), is(getInitialAnswer().getDisplayText()));
    }

    @Test
    public void whenReadOnlyOverrideOptionIsUsed_shouldAllClickableElementsBeDisabled() {
        readOnlyOverride = true;
        when(formEntryPrompt.isReadOnly()).thenReturn(false);
        when(formEntryPrompt.getAnswerText()).thenReturn(getInitialAnswer().getDisplayText());

        ExArbitraryFileWidget widget = getWidget();
        assertThat(widget.binding.exArbitraryFileButton.getVisibility(), is(View.GONE));
        assertThat(widget.binding.exArbitraryFileAnswerText.getVisibility(), is(View.VISIBLE));
        assertThat(widget.binding.exArbitraryFileAnswerText.getText(), is(getInitialAnswer().getDisplayText()));
    }
}
