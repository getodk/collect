package org.odk.collect.android.audio;

import android.media.MediaPlayer;
import android.widget.ImageButton;

import androidx.fragment.app.FragmentActivity;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.odk.collect.android.R;
import org.odk.collect.android.support.RobolectricHelpers;
import org.odk.collect.android.support.TestScreenContext;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.shadows.ShadowMediaPlayer;
import org.robolectric.shadows.util.DataSource;

import java.io.File;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.odk.collect.android.support.RobolectricHelpers.getCreatedFromResId;
import static org.odk.collect.android.support.RobolectricHelpers.setupMediaPlayerDataSource;
import static org.robolectric.Shadows.shadowOf;

@RunWith(RobolectricTestRunner.class)
public class AudioControllerViewIntegrationTest {

    private final MediaPlayer mediaPlayer = new MediaPlayer();

    private FragmentActivity activity;
    private AudioHelper audioHelper;

    @Before
    public void setup() {
        activity = RobolectricHelpers.createThemedActivity(FragmentActivity.class);

        TestScreenContext screenContext = new TestScreenContext(activity);
        audioHelper = new AudioHelper(screenContext, () -> mediaPlayer);
    }

    @Test
    public void canPlayAndPauseAudio() throws Exception {
        String testFile = File.createTempFile("audio", ".mp3").getAbsolutePath();
        final DataSource dataSource = setupMediaPlayerDataSource(testFile);

        AudioControllerView view = new AudioControllerView(activity);
        audioHelper.setAudio(view, testFile, "clip1");

        ImageButton playButton = view.findViewById(R.id.playBtn);
        assertThat(getCreatedFromResId(playButton), equalTo(R.drawable.ic_play_arrow_24dp));

        playButton.performClick();

        assertThat(mediaPlayer.isPlaying(), equalTo(true));
        assertThat(shadowOf(mediaPlayer).getDataSource(), equalTo(dataSource));
        assertThat(getCreatedFromResId(playButton), equalTo(R.drawable.ic_pause_24dp));

        playButton.performClick();

        assertThat(mediaPlayer.isPlaying(), equalTo(false));
        assertThat(shadowOf(mediaPlayer).getState(), equalTo(ShadowMediaPlayer.State.PAUSED));
        assertThat(shadowOf(mediaPlayer).getDataSource(), equalTo(dataSource));
        assertThat(getCreatedFromResId(playButton), equalTo(R.drawable.ic_play_arrow_24dp));

        playButton.performClick();

        assertThat(mediaPlayer.isPlaying(), equalTo(true));
        assertThat(shadowOf(mediaPlayer).getDataSource(), equalTo(dataSource));
        assertThat(getCreatedFromResId(playButton), equalTo(R.drawable.ic_pause_24dp));
    }
}
