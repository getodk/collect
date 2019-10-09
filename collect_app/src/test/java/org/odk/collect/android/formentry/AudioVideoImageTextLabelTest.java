package org.odk.collect.android.formentry;

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
import org.odk.collect.android.formentry.questions.AudioVideoImageTextLabel;
import org.odk.collect.android.support.RobolectricHelpers;
import org.odk.collect.android.support.TestScreenContextActivity;
import org.robolectric.RobolectricTestRunner;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
public class AudioVideoImageTextLabelTest {

    @Rule
    public MockitoRule rule = MockitoJUnit.rule();

    @Mock
    public ReferenceManager referenceManager;

    @Mock
    public AudioHelper audioHelper;

    @Test
    public void withTextView_andAudio_playingAudio_highlightsText() throws Exception {
        MutableLiveData<Boolean> isPlaying = new MutableLiveData<>();
        isPlaying.setValue(false);
        when(audioHelper.setAudio(any(AudioButton.class), any())).thenReturn(isPlaying);

        Activity activity = RobolectricHelpers.createThemedActivity(TestScreenContextActivity.class);

        AudioVideoImageTextLabel audioVideoImageTextLabel = new AudioVideoImageTextLabel(activity);
        audioVideoImageTextLabel.setTextImageVideo(
                new TextView(activity),
                null,
                null,
                null,
                referenceManager);
        audioVideoImageTextLabel.setAudio("file://audio.mp3", audioHelper);

        int originalTextColor = audioVideoImageTextLabel.getLabelTextView().getCurrentTextColor();

        isPlaying.setValue(true);
        int textColor = audioVideoImageTextLabel.getLabelTextView().getCurrentTextColor();
        assertThat(textColor, not(equalTo(originalTextColor)));

        isPlaying.setValue(false);
        textColor = audioVideoImageTextLabel.getLabelTextView().getCurrentTextColor();
        assertThat(textColor, equalTo(originalTextColor));
    }
}
