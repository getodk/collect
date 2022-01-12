package org.odk.collect.android.widgets;

import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.core.util.Pair;

import net.bytebuddy.utility.RandomString;

import org.javarosa.core.model.data.StringData;
import org.junit.Ignore;
import org.junit.Test;
import org.odk.collect.android.R;
import org.odk.collect.android.activities.DrawActivity;
import org.odk.collect.android.formentry.questions.QuestionDetails;
import org.odk.collect.android.support.MockFormEntryPromptBuilder;
import org.odk.collect.android.utilities.QuestionMediaManager;
import org.odk.collect.android.widgets.base.FileWidgetTest;
import org.odk.collect.android.widgets.support.FakeQuestionMediaManager;
import org.odk.collect.android.widgets.support.FakeWaitingForDataRegistry;
import org.odk.collect.shared.TempFiles;

import java.io.File;

import static java.util.Collections.singletonList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.Mockito.when;
import static org.odk.collect.android.support.CollectHelpers.overrideReferenceManager;
import static org.odk.collect.android.support.CollectHelpers.setupFakeReferenceManager;
import static org.robolectric.Shadows.shadowOf;

/**
 *  @author James Knight
 */
public class SignatureWidgetTest extends FileWidgetTest<SignatureWidget> {

    private File currentFile;

    @NonNull
    @Override
    public SignatureWidget createWidget() {
        QuestionMediaManager fakeQuestionMediaManager = new FakeQuestionMediaManager() {
            @Override
            public File getAnswerFile(String fileName) {
                File result;
                if (currentFile == null) {
                    result = super.getAnswerFile(fileName);
                } else {
                    result = fileName.equals(DrawWidgetTest.USER_SPECIFIED_IMAGE_ANSWER) ? currentFile : null;
                }
                return result;
            }
        };
        return new SignatureWidget(activity, new QuestionDetails(formEntryPrompt, readOnlyOverride),
                fakeQuestionMediaManager, new FakeWaitingForDataRegistry(), TempFiles.getPathInTempDir());
    }

    @NonNull
    @Override
    public StringData getNextAnswer() {
        return new StringData(RandomString.make());
    }

    @Test
    public void buttonsShouldLaunchCorrectIntents() {
        stubAllRuntimePermissionsGranted(true);

        Intent intent = getIntentLaunchedByClick(R.id.simple_button);
        assertComponentEquals(activity, DrawActivity.class, intent);
        assertExtraEquals(DrawActivity.OPTION, DrawActivity.OPTION_SIGNATURE, intent);
    }

    @Test
    public void usingReadOnlyOptionShouldMakeAllClickableElementsDisabled() {
        when(formEntryPrompt.isReadOnly()).thenReturn(true);

        assertThat(getSpyWidget().signButton.getVisibility(), is(View.GONE));
    }

    @Test
    public void whenReadOnlyOverrideOptionIsUsed_shouldAllClickableElementsBeDisabled() {
        readOnlyOverride = true;
        when(formEntryPrompt.isReadOnly()).thenReturn(false);

        assertThat(getSpyWidget().signButton.getVisibility(), is(View.GONE));
    }

    @Test
    @Ignore
    public void whenPromptHasDefaultAnswer_showsInImageView() throws Exception {
        String imagePath = File.createTempFile("default", ".bmp").getAbsolutePath();
        overrideReferenceManager(setupFakeReferenceManager(singletonList(
                new Pair<>(DrawWidgetTest.DEFAULT_IMAGE_ANSWER, imagePath)
        )));

        formEntryPrompt = new MockFormEntryPromptBuilder()
                .withAnswerDisplayText(DrawWidgetTest.DEFAULT_IMAGE_ANSWER)
                .build();

        SignatureWidget widget = createWidget();
        ImageView imageView = widget.getImageView();
        assertThat(imageView, notNullValue());
        Drawable drawable = imageView.getDrawable();
        assertThat(drawable, notNullValue());

        String loadedPath = shadowOf(((BitmapDrawable) drawable).getBitmap()).getCreatedFromPath();
        assertThat(loadedPath, equalTo(imagePath));
    }

    @Test
    @Ignore
    public void whenPromptHasCurrentAnswer_showsInImageView() throws Exception {
        String imagePath = File.createTempFile("current", ".bmp").getAbsolutePath();
        currentFile = new File(imagePath);

        formEntryPrompt = new MockFormEntryPromptBuilder()
                .withAnswerDisplayText(DrawWidgetTest.USER_SPECIFIED_IMAGE_ANSWER)
                .build();

        SignatureWidget widget = createWidget();
        ImageView imageView = widget.getImageView();
        assertThat(imageView, notNullValue());
        Drawable drawable = imageView.getDrawable();
        assertThat(drawable, notNullValue());

        String loadedPath = shadowOf(((BitmapDrawable) drawable).getBitmap()).getCreatedFromPath();
        assertThat(loadedPath, equalTo(imagePath));
    }
}
