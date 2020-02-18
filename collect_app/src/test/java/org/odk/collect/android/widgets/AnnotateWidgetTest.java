package org.odk.collect.android.widgets;

import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.NonNull;

import net.bytebuddy.utility.RandomString;

import org.javarosa.core.model.data.StringData;
import org.javarosa.core.reference.ReferenceManager;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.odk.collect.android.R;
import org.odk.collect.android.activities.DrawActivity;
import org.odk.collect.android.formentry.questions.QuestionDetails;
import org.odk.collect.android.injection.config.AppDependencyModule;
import org.odk.collect.android.support.CollectHelpers;
import org.odk.collect.android.support.MockFormEntryPromptBuilder;
import org.odk.collect.android.support.RobolectricHelpers;
import org.odk.collect.android.widgets.base.FileWidgetTest;

import java.io.File;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;
import static org.odk.collect.android.support.CollectHelpers.createFakeBitmapReference;
import static org.robolectric.Shadows.shadowOf;

/**
 * @author James Knight
 */
public class AnnotateWidgetTest extends FileWidgetTest<AnnotateWidget> {

    @Mock
    File file;

    @Mock
    ReferenceManager referenceManager;

    @Before
    public void setup() throws Exception {
        CollectHelpers.setupFakeReferenceManager(referenceManager);
        RobolectricHelpers.overrideAppDependencyModule(new AppDependencyModule() {
            @Override
            public ReferenceManager providesReferenceManager() {
                return referenceManager;
            }
        });
    }

    @NonNull
    @Override
    public AnnotateWidget createWidget() {
        return new AnnotateWidget(activity, new QuestionDetails(formEntryPrompt, "formAnalyticsID"));
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

        assertThat(getWidget().captureButton.getVisibility(), is(View.GONE));
        assertThat(getWidget().chooseButton.getVisibility(), is(View.GONE));
        assertThat(getWidget().annotateButton.getVisibility(), is(View.GONE));
    }

    @Test
    public void whenPromptHasDefaultAnswer_showsInImageView() throws Exception {
        reset(referenceManager);
        String referenceURI = "jr://images/referenceURI";
        String imagePath = createFakeBitmapReference(referenceManager, referenceURI, "blah");

        formEntryPrompt = new MockFormEntryPromptBuilder()
                .withAnswerDisplayText(referenceURI)
                .build();

        AnnotateWidget widget = createWidget();
        ImageView imageView = widget.getImageView();
        assertThat(imageView, notNullValue());
        Drawable drawable = imageView.getDrawable();
        assertThat(drawable, notNullValue());

        String loadedImagePath = shadowOf(((BitmapDrawable) drawable).getBitmap()).getCreatedFromPath();
        assertThat(loadedImagePath, equalTo(imagePath));
    }
}