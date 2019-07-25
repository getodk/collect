package org.odk.collect.android.views;

import android.widget.TextView;

import androidx.fragment.app.FragmentActivity;

import org.javarosa.core.reference.InvalidReferenceException;
import org.javarosa.core.reference.Reference;
import org.javarosa.core.reference.ReferenceManager;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.odk.collect.android.R;
import org.odk.collect.android.audio.AudioButtonManager;
import org.odk.collect.android.support.RobolectricHelpers;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.shadows.ShadowMediaPlayer;
import org.robolectric.shadows.util.DataSource;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
public class MediaLayoutTest {

    @Rule
    public MockitoRule rule = MockitoJUnit.rule();

    @Mock
    public ReferenceManager referenceManager;

    @Test
    public void withTextView_andAudio_playingAudio_highlightsText() throws Exception {
        FragmentActivity activity = RobolectricHelpers.createThemedActivity();
        setupAudioFile("file://audio.mp3", referenceManager);

        MediaLayout mediaLayout = new MediaLayout(activity);
        mediaLayout.setAVT(
                new TextView(activity),
                "file://audio.mp3",
                null,
                null,
                null,
                referenceManager,
                new AudioButtonManager());

        int originalTextColor = mediaLayout.getTextView().getCurrentTextColor();

        mediaLayout.findViewById(R.id.audioButton).performClick();
        int textColor = mediaLayout.getTextView().getCurrentTextColor();
        assertThat(textColor, not(equalTo(originalTextColor)));

        mediaLayout.findViewById(R.id.audioButton).performClick();
        textColor = mediaLayout.getTextView().getCurrentTextColor();
        assertThat(textColor, equalTo(originalTextColor));
    }

    private static void setupAudioFile(String audioURI, ReferenceManager referenceManager) throws InvalidReferenceException {
        ShadowMediaPlayer.addMediaInfo(DataSource.toDataSource(audioURI), new ShadowMediaPlayer.MediaInfo());
        Reference reference = mock(Reference.class);
        when(reference.getLocalURI()).thenReturn(audioURI);
        when(referenceManager.deriveReference(audioURI)).thenReturn(reference);
    }
}
