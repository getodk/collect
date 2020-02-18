package org.odk.collect.android.widgets;

import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.NonNull;

import net.bytebuddy.utility.RandomString;

import org.javarosa.core.model.data.StringData;
import org.javarosa.core.reference.ReferenceManager;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.odk.collect.android.R;
import org.odk.collect.android.activities.DrawActivity;
import org.odk.collect.android.formentry.questions.QuestionDetails;
import org.odk.collect.android.injection.config.AppDependencyModule;
import org.odk.collect.android.support.CollectHelpers;
import org.odk.collect.android.support.MockFormEntryPromptBuilder;
import org.odk.collect.android.support.RobolectricHelpers;
import org.odk.collect.android.widgets.base.FileWidgetTest;
import org.robolectric.RobolectricTestRunner;

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
@RunWith(RobolectricTestRunner.class)
public class DrawWidgetTest extends FileWidgetTest<DrawWidget> {

    @Mock
    File file;

    @Mock
    ReferenceManager referenceManager;

    private String fileName;

    @NonNull
    @Override
    public DrawWidget createWidget() {
        return new DrawWidget(activity, new QuestionDetails(formEntryPrompt, "formAnalyticsID"));
    }

    @NonNull
    @Override
    public StringData getNextAnswer() {
        return new StringData(fileName);
    }

    @Override
    public Object createBinaryData(StringData answerData) {
        return file;
    }

    @Before
    public void setUp() throws Exception {
        super.setUp();
        fileName = RandomString.make();
        CollectHelpers.setupFakeReferenceManager(referenceManager);
        RobolectricHelpers.overrideAppDependencyModule(new AppDependencyModule() {
            @Override
            public ReferenceManager providesReferenceManager() {
                return referenceManager;
            }
        });
    }

    @Override
    protected void prepareForSetAnswer() {

        when(file.exists()).thenReturn(true);
        when(file.getName()).thenReturn(fileName);
    }

    @Test
    public void buttonsShouldLaunchCorrectIntents() {
        stubAllRuntimePermissionsGranted(true);

        Intent intent = getIntentLaunchedByClick(R.id.simple_button);
        assertComponentEquals(activity, DrawActivity.class, intent);
        assertExtraEquals(DrawActivity.OPTION, DrawActivity.OPTION_DRAW, intent);
    }

    @Test
    public void usingReadOnlyOptionShouldMakeAllClickableElementsDisabled() {
        when(formEntryPrompt.isReadOnly()).thenReturn(true);

        assertThat(getWidget().drawButton.getVisibility(), is(View.GONE));
    }

    @Test
    public void whenPromptHasDefaultAnswer_showsInImageView() throws Exception {
        reset(referenceManager);
        String referenceURI = "jr://images/referenceURI";
        String imagePath = createFakeBitmapReference(referenceManager, referenceURI, "blah");

        formEntryPrompt = new MockFormEntryPromptBuilder()
                .withAnswerDisplayText(referenceURI)
                .build();

        DrawWidget widget = createWidget();
        ImageView imageView = widget.getImageView();
        assertThat(imageView, notNullValue());
        Drawable drawable = imageView.getDrawable();
        assertThat(drawable, notNullValue());

        String loadedImagePath = shadowOf(((BitmapDrawable) drawable).getBitmap()).getCreatedFromPath();
        assertThat(loadedImagePath, equalTo(imagePath));
    }
}