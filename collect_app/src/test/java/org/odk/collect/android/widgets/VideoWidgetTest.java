package org.odk.collect.android.widgets;

import android.content.Intent;
import android.net.Uri;
import android.provider.MediaStore;
import androidx.annotation.NonNull;

import net.bytebuddy.utility.RandomString;

import org.javarosa.core.model.data.StringData;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.odk.collect.android.R;
import org.odk.collect.android.utilities.FileUtil;
import org.odk.collect.android.utilities.MediaUtil;
import org.odk.collect.android.widgets.base.FileWidgetTest;

import java.io.File;

import static org.mockito.Mockito.when;

/**
 * @author James Knight
 */
public class VideoWidgetTest extends FileWidgetTest<VideoWidget> {

    @Mock
    Uri uri;

    @Mock
    MediaUtil mediaUtil;

    @Mock
    FileUtil fileUtil;

    @Mock
    File file;

    private String destinationName;

    @NonNull
    @Override
    public VideoWidget createWidget() {
        return new VideoWidget(activity, formEntryPrompt, fileUtil, mediaUtil);
    }

    @NonNull
    @Override
    public StringData getNextAnswer() {
        return new StringData(destinationName);
    }

    @Override
    public Object createBinaryData(StringData answerData) {
        return uri;
    }

    @Before
    public void setUp() throws Exception {
        super.setUp();
        destinationName = RandomString.make();
    }

    @Override
    public void settingANewAnswerShouldCallDeleteMediaToRemoveTheOldFile() {
        prepareForSetAnswer();
        super.settingANewAnswerShouldCallDeleteMediaToRemoveTheOldFile();
    }

    @Override
    public void getAnswerShouldReturnCorrectAnswerAfterBeingSet() {
        prepareForSetAnswer();
        super.getAnswerShouldReturnCorrectAnswerAfterBeingSet();
    }

    @Override
    public void settingANewAnswerShouldRemoveTheOldAnswer() {
        prepareForSetAnswer();
        super.settingANewAnswerShouldRemoveTheOldAnswer();
    }

    public void prepareForSetAnswer() {
        when(formEntryPrompt.isReadOnly()).thenReturn(false);

        when(mediaUtil.getPathFromUri(
                activity,
                uri,
                MediaStore.Video.Media.DATA)

        ).thenReturn(String.format("%s.mp4", RandomString.make()));

        when(fileUtil.getRandomFilename()).thenReturn(destinationName);
        when(fileUtil.getFileAtPath(String.format("/%s.mp4", destinationName)))
                .thenReturn(file);

        when(file.getName()).thenReturn(destinationName);
    }

    @Test
    public void buttonsShouldLaunchCorrectIntents() {
        stubAllRuntimePermissionsGranted(true);

        Intent intent = getIntentLaunchedByClick(R.id.capture_video);
        assertActionEquals(MediaStore.ACTION_VIDEO_CAPTURE, intent);

        intent = getIntentLaunchedByClick(R.id.choose_video);
        assertActionEquals(Intent.ACTION_GET_CONTENT, intent);
        assertTypeEquals("video/*", intent);

        intent = getIntentLaunchedByClick(R.id.play_video);
        assertActionEquals(Intent.ACTION_VIEW, intent);
        assertTypeEquals("video/*", intent);
    }

    @Test
    public void buttonsShouldNotLaunchIntentsWhenPermissionsDenied() {
        stubAllRuntimePermissionsGranted(false);

        assertIntentNotStarted(activity, getIntentLaunchedByClick(R.id.capture_video));
    }
}