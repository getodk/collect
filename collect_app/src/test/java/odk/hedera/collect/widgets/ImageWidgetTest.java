package odk.hedera.collect.widgets;

import android.content.Intent;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.core.util.Pair;

import net.bytebuddy.utility.RandomString;

import org.javarosa.core.model.data.StringData;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.odk.hedera.collect.R;
import odk.hedera.collect.formentry.questions.QuestionDetails;
import org.odk.hedera.collect.support.MockFormEntryPromptBuilder;
import odk.hedera.collect.widgets.base.FileWidgetTest;

import java.io.File;

import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.when;
import static org.odk.hedera.collect.support.CollectHelpers.overrideReferenceManager;
import static org.odk.hedera.collect.support.CollectHelpers.setupFakeReferenceManager;

/**
 * @author James Knight
 */
public class ImageWidgetTest extends FileWidgetTest<ImageWidget> {

    @Mock
    File file;

    private String fileName;

    @NonNull
    @Override
    public ImageWidget createWidget() {
        return new ImageWidget(activity, new QuestionDetails(formEntryPrompt, "formAnalyticsID"));
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
    }

    @Override
    protected void prepareForSetAnswer() {

        when(file.exists()).thenReturn(true);
        when(file.getName()).thenReturn(fileName);
    }

    @Test
    public void buttonsShouldLaunchCorrectIntents() {
        stubAllRuntimePermissionsGranted(true);

        Intent intent = getIntentLaunchedByClick(R.id.capture_image);
        assertActionEquals(MediaStore.ACTION_IMAGE_CAPTURE, intent);

        intent = getIntentLaunchedByClick(R.id.choose_image);
        assertActionEquals(Intent.ACTION_GET_CONTENT, intent);
        assertTypeEquals("image/*", intent);
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
    }

    @Test
    public void whenPromptHasDefaultAnswer_doesNotShow() throws Exception {
        String defaultImagePath = File.createTempFile("blah", ".bmp").getAbsolutePath();
        overrideReferenceManager(setupFakeReferenceManager(asList(
                new Pair<>("jr://images/referenceURI", defaultImagePath)
        )));

        formEntryPrompt = new MockFormEntryPromptBuilder()
                .withAnswerDisplayText("jr://images/referenceURI")
                .build();

        ImageWidget widget = createWidget();
        ImageView imageView = widget.getImageView();
        assertThat(imageView, nullValue());
    }
}