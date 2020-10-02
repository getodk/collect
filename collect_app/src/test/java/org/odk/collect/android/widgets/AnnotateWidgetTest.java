package org.odk.collect.android.widgets;

import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.core.util.Pair;

import net.bytebuddy.utility.RandomString;

import org.javarosa.core.model.data.StringData;
import org.junit.Test;
import org.mockito.Mock;
import org.odk.collect.android.R;
import org.odk.collect.android.activities.DrawActivity;
import org.odk.collect.android.formentry.questions.QuestionDetails;
import org.odk.collect.android.support.MockFormEntryPromptBuilder;
import org.odk.collect.android.widgets.base.FileWidgetTest;
import org.odk.collect.android.widgets.support.FakeQuestionMediaManager;
import org.odk.collect.android.widgets.support.FakeWaitingForDataRegistry;

import java.io.File;

import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.Mockito.when;
import static org.odk.collect.android.support.CollectHelpers.overrideReferenceManager;
import static org.odk.collect.android.support.CollectHelpers.setupFakeReferenceManager;
import static org.robolectric.Shadows.shadowOf;

/**
 * @author James Knight
 */
public class AnnotateWidgetTest extends FileWidgetTest<AnnotateWidget> {

    @Mock
    File file;

    @NonNull
    @Override
    public AnnotateWidget createWidget() {
        return new AnnotateWidget(activity, new QuestionDetails(formEntryPrompt, "formAnalyticsID"),
                new FakeQuestionMediaManager(), new FakeWaitingForDataRegistry());
    }

    @NonNull
    @Override
    public StringData getNextAnswer() {
        return new StringData(RandomString.make());
    }

    @Override
    public Object createBinaryData(StringData answerData) {
        when(file.exists()).thenReturn(true);
        when(file.getName()).thenReturn(answerData.getDisplayText());

        return file;
    }

    @Test
    public void buttonsShouldLaunchCorrectIntents() {
        stubAllRuntimePermissionsGranted(true);

        Intent intent = getIntentLaunchedByClick(R.id.capture_image);
        assertActionEquals(MediaStore.ACTION_IMAGE_CAPTURE, intent);

        intent = getIntentLaunchedByClick(R.id.choose_image);
        assertActionEquals(Intent.ACTION_GET_CONTENT, intent);

        intent = getIntentLaunchedByClick(R.id.markup_image);
        assertComponentEquals(activity, DrawActivity.class, intent);
        assertExtraEquals(DrawActivity.OPTION, DrawActivity.OPTION_ANNOTATE, intent);
    }

    @Test
    public void buttonsShouldNotLaunchIntentsWhenPermissionsDenied() {
        stubAllRuntimePermissionsGranted(false);

        assertIntentNotStarted(activity, getIntentLaunchedByClick(R.id.capture_image));
    }

    @Test
    public void usingReadOnlyOptionShouldMakeAllClickableElementsDisabled() {
        when(formEntryPrompt.isReadOnly()).thenReturn(true);

        assertThat(getSpyWidget().captureButton.getVisibility(), is(View.GONE));
        assertThat(getSpyWidget().chooseButton.getVisibility(), is(View.GONE));
        assertThat(getSpyWidget().annotateButton.getVisibility(), is(View.GONE));
    }

    @Test
    public void whenPromptHasDefaultAnswer_showsInImageView() throws Exception {
        String defaultImagePath = File.createTempFile("blah", ".bmp").getAbsolutePath();
        overrideReferenceManager(setupFakeReferenceManager(asList(
                new Pair<>("jr://images/referenceURI", defaultImagePath)
        )));

        formEntryPrompt = new MockFormEntryPromptBuilder()
                .withAnswerDisplayText("jr://images/referenceURI")
                .build();

        AnnotateWidget widget = createWidget();
        ImageView imageView = widget.getImageView();
        assertThat(imageView, notNullValue());
        Drawable drawable = imageView.getDrawable();
        assertThat(drawable, notNullValue());

        String loadedImagePath = shadowOf(((BitmapDrawable) drawable).getBitmap()).getCreatedFromPath();
        assertThat(loadedImagePath, equalTo(defaultImagePath));
    }

    @Test
    public void markupButtonShouldBeDisabledIfImageAbsent() throws Exception {
        String wrongDefaultPath = "wrong_path";
        overrideReferenceManager(setupFakeReferenceManager(asList(
                new Pair<>("jr://images/referenceURI", wrongDefaultPath)
        )));

        formEntryPrompt = new MockFormEntryPromptBuilder()
                .withAnswerDisplayText("jr://images/referenceURI")
                .build();

        assertThat(getWidget().annotateButton.isEnabled(), is(false));
    }
}