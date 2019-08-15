package org.odk.collect.android.views;

import android.app.Activity;
import android.widget.TextView;

import androidx.lifecycle.MutableLiveData;

import org.javarosa.core.reference.ReferenceManager;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.odk.collect.android.audio.AudioButton;
import org.odk.collect.android.audio.AudioHelper;
import org.odk.collect.android.support.RobolectricHelpers;
import org.odk.collect.android.support.TestScreenContextActivity;
import org.robolectric.RobolectricTestRunner;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.odk.collect.android.support.Helpers.setupMockReference;

@RunWith(RobolectricTestRunner.class)
public class MediaLayoutTest {

    @Rule
    public MockitoRule rule = MockitoJUnit.rule();

    @Mock
    public ReferenceManager referenceManager;

    @Mock
    public AudioHelper audioHelper;

    @Test
    public void withTextView_andAudio_playingAudio_highlightsText() throws Exception {
        setupMockReference("file://audio.mp3", referenceManager);

        MutableLiveData<Boolean> isPlaying = new MutableLiveData<>();
        isPlaying.setValue(false);
        when(audioHelper.setAudio(any(AudioButton.class), any(), any())).thenReturn(isPlaying);

        Activity activity = RobolectricHelpers.createThemedActivity(TestScreenContextActivity.class);

        MediaLayout mediaLayout = new MediaLayout(activity);
        mediaLayout.setAVT(
                new TextView(activity),
                "file://audio.mp3",
                null,
                null,
                null,
                referenceManager,
                audioHelper);

        int originalTextColor = mediaLayout.getTextView().getCurrentTextColor();

        isPlaying.setValue(true);
        int textColor = mediaLayout.getTextView().getCurrentTextColor();
        assertThat(textColor, not(equalTo(originalTextColor)));

        isPlaying.setValue(false);
        textColor = mediaLayout.getTextView().getCurrentTextColor();
        assertThat(textColor, equalTo(originalTextColor));
    }
}
