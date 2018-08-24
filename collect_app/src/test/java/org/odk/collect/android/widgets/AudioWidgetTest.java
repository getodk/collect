package org.odk.collect.android.widgets;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.ViewGroup;

import net.bytebuddy.utility.RandomString;

import org.javarosa.core.model.data.StringData;
import org.mockito.Mock;
import org.odk.collect.android.utilities.FileUtil;
import org.odk.collect.android.utilities.MediaUtil;
import org.odk.collect.android.widgets.base.FileWidgetTest;
import org.robolectric.RuntimeEnvironment;

import java.io.File;

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
        return new AudioWidget(RuntimeEnvironment.application, formEntryPrompt, fileUtil, mediaUtil, audioController);
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
}