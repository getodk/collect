package org.odk.collect.android.widgets;

import android.view.View;

import androidx.annotation.NonNull;

import net.bytebuddy.utility.RandomString;

import org.javarosa.core.model.data.StringData;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.odk.collect.android.formentry.questions.QuestionDetails;
import org.odk.collect.android.utilities.ApplicationConstants;
import org.odk.collect.android.utilities.MediaUtils;
import org.odk.collect.android.widgets.base.FileWidgetTest;
import org.odk.collect.android.widgets.support.FakeQuestionMediaManager;
import org.odk.collect.android.widgets.support.FakeWaitingForDataRegistry;

import java.io.File;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ArbitraryFileWidgetTest extends FileWidgetTest<ArbitraryFileWidget> {
    @Mock
    File file;

    @Mock
    MediaUtils mediaUtils;

    private String destinationName;

    @Before
    public void setUp() throws Exception {
        super.setUp();
        destinationName = RandomString.make();
    }

    @Override
    public Object createBinaryData(StringData answerData) {
        return file;
    }

    @NonNull
    @Override
    public ArbitraryFileWidget createWidget() {
        return new ArbitraryFileWidget(activity, new QuestionDetails(formEntryPrompt, "formAnalyticsID", readOnlyOverride),
                mediaUtils, new FakeQuestionMediaManager(), new FakeWaitingForDataRegistry());
    }

    @NonNull
    @Override
    public StringData getNextAnswer() {
        return new StringData(destinationName);
    }

    @Test
    public void buttonsShouldLaunchCorrectIntents() {
        stubAllRuntimePermissionsGranted(true);

        getSpyWidget().binding.arbitraryFileButton.performClick();
        verify(mediaUtils).pickFile(activity, "*/*", ApplicationConstants.RequestCodes.ARBITRARY_FILE_CHOOSER);
    }

    @Test
    public void usingReadOnlyOptionShouldMakeAllClickableElementsDisabled() {
        when(formEntryPrompt.isReadOnly()).thenReturn(true);

        assertThat(getSpyWidget().binding.arbitraryFileButton.getVisibility(), is(View.GONE));
    }

    @Test
    public void whenReadOnlyOverrideOptionIsUsed_shouldAllClickableElementsBeDisabled() {
        when(formEntryPrompt.isReadOnly()).thenReturn(true);

        assertThat(getSpyWidget().binding.arbitraryFileButton.getVisibility(), is(View.GONE));
    }

    public void prepareAnswerFile() {
        when(file.exists()).thenReturn(true);
        when(file.getName()).thenReturn(destinationName);
    }
}
