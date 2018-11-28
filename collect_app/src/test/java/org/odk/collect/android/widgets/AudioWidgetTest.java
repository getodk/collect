package org.odk.collect.android.widgets;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import net.bytebuddy.utility.RandomString;

import org.javarosa.core.model.data.StringData;
import org.junit.Test;
import org.mockito.Mock;
import org.odk.collect.android.R;
import org.odk.collect.android.utilities.FileUtil;
import org.odk.collect.android.utilities.MediaUtil;
import org.odk.collect.android.widgets.base.FileWidgetTest;

import java.io.File;

import static android.provider.MediaStore.Audio.Media.RECORD_SOUND_ACTION;
import static android.provider.MediaStore.EXTRA_OUTPUT;
import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author James Knight
 */
public class AudioWidgetTest extends FileWidgetTest<AudioWidget> {

    @Mock
    Uri uri;

    @Mock
    MediaUtil mediaUtil;

    @Mock
    FileUtil fileUtil;

    @Mock
    AudioController audioController;

    private String destinationName;

    @NonNull
    @Override
    public AudioWidget createWidget() {
        when(audioController.getPlayerLayout(any(ViewGroup.class))).thenReturn(mock(View.class));
        return new AudioWidget(activity, formEntryPrompt, fileUtil, mediaUtil, audioController);
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

    @Override
    public void setUp() throws Exception {
        super.setUp();
        destinationName = RandomString.make();
    }

    @Override
    protected void prepareForSetAnswer() {
        when(formEntryPrompt.isReadOnly()).thenReturn(false);

        when(mediaUtil.getPathFromUri(any(Context.class), any(Uri.class), any(String.class)))
                .thenReturn(String.format("%s.mp3", RandomString.make()));

        when(fileUtil.getRandomFilename()).thenReturn(destinationName);

        File firstFile = mock(File.class);

        when(fileUtil.getFileAtPath(String.format("/%s.mp3", destinationName)))
                .thenReturn(firstFile);

        when(firstFile.exists()).thenReturn(true);
        when(firstFile.getName()).thenReturn(destinationName);
    }

    @Override
    protected Intent getExpectedIntent(Button clickedButton, boolean permissionGranted) {
        Intent intent = null;

        switch (clickedButton.getId()) {
            case R.id.capture_audio:
                if (permissionGranted) {
                    intent = new Intent(RECORD_SOUND_ACTION);
                    intent.putExtra(EXTRA_OUTPUT, MediaStore.Audio.Media.EXTERNAL_CONTENT_URI.toString());
                }
                break;
            case R.id.choose_sound:

                /* We aren't checking for storage permissions as without that permission
                 * FormEntryActivity cannot be started */

                intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("audio/*");
                break;
        }
        return intent;
    }

    @Test
    public void buttonsShouldLaunchCorrectIntents() {
        stubAllRuntimePermissionsGranted(true);

        Intent intent = getIntentLaunchedByClick(R.id.capture_audio);
        assertActionEquals(MediaStore.Audio.Media.RECORD_SOUND_ACTION, intent);

        intent = getIntentLaunchedByClick(R.id.choose_sound);
        assertActionEquals(Intent.ACTION_GET_CONTENT, intent);
    }

    @Test
    public void buttonsShouldNotLaunchIntentsWhenPermissionsDenied() {
        stubAllRuntimePermissionsGranted(false);

        assertNull(getIntentLaunchedByClick(R.id.capture_audio));
    }
}