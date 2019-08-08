package org.odk.collect.android.audio;

import android.media.MediaPlayer;
import android.widget.ImageButton;

import androidx.fragment.app.FragmentActivity;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.odk.collect.android.R;
import org.odk.collect.android.support.FakeScheduler;
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
import static org.robolectric.shadows.ShadowView.innerText;

@RunWith(RobolectricTestRunner.class)
public class AudioControllerViewIntegrationTest {

    private final MediaPlayer mediaPlayer = new MediaPlayer();
    private final FakeScheduler fakeScheduler = new FakeScheduler();

    private FragmentActivity activity;
    private AudioHelper audioHelper;

    @Before
    public void setup() {
        activity = RobolectricHelpers.createThemedActivity(FragmentActivity.class);

        TestScreenContext screenContext = new TestScreenContext(activity);
        audioHelper = new AudioHelper(screenContext, () -> mediaPlayer, fakeScheduler);
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

    @Test
    public void showsPositionAndTotalDurationOfClip() throws Exception {
        String testFile = File.createTempFile("audio", ".mp3").getAbsolutePath();
        setupMediaPlayerDataSource(testFile);

        AudioControllerView view = new AudioControllerView(activity);
        audioHelper.setAudio(view, testFile, "clip1");

        assertThat(innerText(view.findViewById(R.id.currentDuration)), equalTo("00:00"));
        assertThat(innerText(view.findViewById(R.id.totalDuration)), equalTo("00:14"));

        view.findViewById(R.id.playBtn).performClick();

        shadowOf(mediaPlayer).setCurrentPosition(1005);
        fakeScheduler.runTask();
        assertThat(innerText(view.findViewById(R.id.currentDuration)), equalTo("00:01"));

        shadowOf(mediaPlayer).setCurrentPosition(12404);
        fakeScheduler.runTask();
        assertThat(innerText(view.findViewById(R.id.currentDuration)), equalTo("00:12"));

        shadowOf(mediaPlayer).setCurrentPosition(322450);
        fakeScheduler.runTask();
        assertThat(innerText(view.findViewById(R.id.currentDuration)), equalTo("05:22"));
    }

    @Test
    public void resetsBackToBeginningWhenPlaybackFinishes() throws Exception {
        String testFile = File.createTempFile("audio", ".mp3").getAbsolutePath();
        setupMediaPlayerDataSource(testFile);

        AudioControllerView view = new AudioControllerView(activity);
        audioHelper.setAudio(view, testFile, "clip1");

        view.findViewById(R.id.playBtn).performClick();

        shadowOf(mediaPlayer).invokeCompletionListener();

        fakeScheduler.runTask();
        assertThat(getCreatedFromResId(view.findViewById(R.id.playBtn)), equalTo(R.drawable.ic_play_arrow_24dp));
        assertThat(innerText(view.findViewById(R.id.currentDuration)), equalTo("00:00"));
    }

    @Test
    public void whenThereAreTwoViews_showsCorrectDuration() throws Exception {
        String testFile1 = File.createTempFile("audio", ".mp3").getAbsolutePath();
        setupMediaPlayerDataSource(testFile1);

        String testFile2 = File.createTempFile("audio", ".mp3").getAbsolutePath();
        setupMediaPlayerDataSource(testFile2);

        AudioControllerView view1 = new AudioControllerView(activity);
        audioHelper.setAudio(view1, testFile1, "clip1");

        AudioControllerView view2 = new AudioControllerView(activity);
        audioHelper.setAudio(view2, testFile1, "clip2");

        view1.findViewById(R.id.playBtn).performClick();

        shadowOf(mediaPlayer).setCurrentPosition(1005);
        fakeScheduler.runTask();

        assertThat(innerText(view1.findViewById(R.id.currentDuration)), equalTo("00:01"));
        assertThat(innerText(view2.findViewById(R.id.currentDuration)), equalTo("00:00"));
    }
}
