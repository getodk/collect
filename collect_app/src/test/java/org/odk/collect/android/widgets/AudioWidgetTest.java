package org.odk.collect.android.widgets;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;

import org.javarosa.core.model.data.StringData;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.odk.collect.android.BuildConfig;
import org.odk.collect.android.utilities.MediaUtil;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * @author James Knight
 */
@Config(constants = BuildConfig.class)
@RunWith(RobolectricTestRunner.class)
public class AudioWidgetTest extends BinaryNameWidgetTest<AudioWidget> {

    @Mock
    Uri uri;

    @Mock
    MediaUtil mediaUtil;

    public AudioWidgetTest() {
        super(AudioWidget.class);
    }

    @NonNull
    @Override
    public AudioWidget createWidget() {
        AudioWidget audioWidget = new AudioWidget(RuntimeEnvironment.application, formEntryPrompt);
        audioWidget.setMediaUtil(mediaUtil);

        return audioWidget;
    }

    @Override
    Object createBinaryData(StringData answerData) {
        return uri;
    }

    @Before
    public void setUp() throws Exception {
        super.setUp();
        when(formEntryPrompt.isReadOnly()).thenReturn(false);
    }

    @Override
    public void settingANewAnswerShouldCallDeleteMediaToRemoveTheOldFile() {
        when(mediaUtil.getPathFromUri(any(Context.class), any(Uri.class), any(String.class)))
                .thenReturn(".");

        super.settingANewAnswerShouldCallDeleteMediaToRemoveTheOldFile();
    }
}