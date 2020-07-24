package org.odk.collect.android.audio;

import android.media.MediaPlayer;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.SeekBar;

import androidx.fragment.app.FragmentActivity;

import org.jetbrains.annotations.NotNull;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.odk.collect.android.R;
import org.odk.collect.android.support.FakeLifecycleOwner;
import org.odk.collect.android.support.FakeScheduler;
import org.odk.collect.android.support.RobolectricHelpers;
import org.odk.collect.android.support.SwipableParentActivity;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.shadows.ShadowMediaPlayer;
import org.robolectric.shadows.util.DataSource;

import java.io.File;
import java.io.IOException;

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
        activity = RobolectricHelpers.createThemedActivity(SwipableParentActivity.class);

        audioHelper = new AudioHelper(activity, new FakeLifecycleOwner(), fakeScheduler, () -> mediaPlayer);
    }

    @Test
    public void canPlayAndPauseAudio() throws Exception {
        String testFile = File.createTempFile("audio", ".mp3").getAbsolutePath();
        final DataSource dataSource = setupMediaPlayerDataSource(testFile);

        AudioControllerView view = new AudioControllerView(activity);
        audioHelper.setAudio(view, new Clip("clip1", testFile));

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
    public void showsPositionAndTotalDuration() throws Exception {
        AudioControllerView view = createAndSetupView("clip1", 322450);

        final View currentDuration = view.findViewById(R.id.currentDuration);

        assertThat(innerText(currentDuration), equalTo("00:00"));
        assertThat(innerText(view.findViewById(R.id.totalDuration)), equalTo("05:22"));

        view.findViewById(R.id.playBtn).performClick();
        assertThat(innerText(currentDuration), equalTo("00:00"));

        shadowOf(mediaPlayer).setCurrentPosition(1005);
        fakeScheduler.runForeground();
        assertThat(innerText(currentDuration), equalTo("00:01"));

        shadowOf(mediaPlayer).setCurrentPosition(12404);
        fakeScheduler.runForeground();
        assertThat(innerText(currentDuration), equalTo("00:12"));

        shadowOf(mediaPlayer).setCurrentPosition(322450);
        fakeScheduler.runForeground();
        assertThat(innerText(currentDuration), equalTo("05:22"));

        view.findViewById(R.id.playBtn).performClick(); // Make sure duration remains when paused
        fakeScheduler.runForeground();
        assertThat(innerText(currentDuration), equalTo("05:22"));
    }

    @Test
    public void showsPositionOnProgressBar() throws Exception {
        AudioControllerView view = createAndSetupView("clip1", 322450);

        final ProgressBar seekBar = view.findViewById(R.id.seekBar);
        view.findViewById(R.id.playBtn).performClick();
        assertThat(seekBar.getProgress(), equalTo(0));

        shadowOf(mediaPlayer).setCurrentPosition(1005);
        fakeScheduler.runForeground();
        assertThat(seekBar.getProgress(), equalTo(1005));

        shadowOf(mediaPlayer).setCurrentPosition(322450);
        fakeScheduler.runForeground();
        assertThat(seekBar.getProgress(), equalTo(322450));

        view.findViewById(R.id.playBtn).performClick(); // Make sure duration remains when paused
        fakeScheduler.runForeground();
        assertThat(seekBar.getProgress(), equalTo(322450));
    }

    @Test
    public void canSkipForwardAndBackwards() throws Exception {
        AudioControllerView view = createAndSetupView("clip1", 14000);
        view.findViewById(R.id.playBtn).performClick();

        view.findViewById(R.id.fastForwardBtn).performClick();
        fakeScheduler.runForeground();
        assertThat(mediaPlayer.isPlaying(), equalTo(true));
        assertThat(mediaPlayer.getCurrentPosition(), equalTo(5000));

        view.findViewById(R.id.fastForwardBtn).performClick();
        fakeScheduler.runForeground();
        assertThat(mediaPlayer.getCurrentPosition(), equalTo(10000));

        view.findViewById(R.id.fastRewindBtn).performClick();
        fakeScheduler.runForeground();
        assertThat(mediaPlayer.getCurrentPosition(), equalTo(5000));

        view.findViewById(R.id.fastForwardBtn).performClick();
        fakeScheduler.runForeground();
        assertThat(mediaPlayer.getCurrentPosition(), equalTo(10000));

        view.findViewById(R.id.fastForwardBtn).performClick();
        fakeScheduler.runForeground();
        assertThat(mediaPlayer.isPlaying(), equalTo(false));
        assertThat(mediaPlayer.getCurrentPosition(), equalTo(14000));
    }

    @Test
    public void swipingPosition_whenPlaying_pausesAndThenResumes() throws Exception {
        AudioControllerView view = createAndSetupView("clip1", 14000);
        view.findViewById(R.id.playBtn).performClick();

        SeekBar seekBar = view.findViewById(R.id.seekBar);
        final View currentDuration = view.findViewById(R.id.currentDuration);

        shadowOf(seekBar).getOnSeekBarChangeListener().onStartTrackingTouch(seekBar);
        fakeScheduler.runForeground();
        assertThat(mediaPlayer.isPlaying(), equalTo(false)); // Check seeking pauses playback

        shadowOf(seekBar).getOnSeekBarChangeListener().onProgressChanged(seekBar, 7000, true);
        fakeScheduler.runForeground();
        assertThat(innerText(currentDuration), equalTo("00:07"));

        shadowOf(seekBar).getOnSeekBarChangeListener().onProgressChanged(seekBar, 8000, true);
        fakeScheduler.runForeground();
        assertThat(innerText(currentDuration), equalTo("00:08"));

        shadowOf(seekBar).getOnSeekBarChangeListener().onStopTrackingTouch(seekBar);
        fakeScheduler.runForeground();
        assertThat(mediaPlayer.isPlaying(), equalTo(true));
        assertThat(mediaPlayer.getCurrentPosition(), equalTo(8000));
    }

    @Test
    public void swipingPosition_whenIdle_justChangesPosition() throws Exception {
        AudioControllerView view = createAndSetupView("clip1", 14000);
        SeekBar seekBar = view.findViewById(R.id.seekBar);
        View currentDuration = view.findViewById(R.id.currentDuration);

        shadowOf(seekBar).getOnSeekBarChangeListener().onStartTrackingTouch(seekBar);

        shadowOf(seekBar).getOnSeekBarChangeListener().onProgressChanged(seekBar, 7000, true);
        assertThat(innerText(currentDuration), equalTo("00:07"));

        shadowOf(seekBar).getOnSeekBarChangeListener().onProgressChanged(seekBar, 8000, true);
        assertThat(innerText(currentDuration), equalTo("00:08"));

        shadowOf(seekBar).getOnSeekBarChangeListener().onStopTrackingTouch(seekBar);
        assertThat(mediaPlayer.isPlaying(), equalTo(false));

        view.findViewById(R.id.playBtn).performClick();
        fakeScheduler.runForeground();
        assertThat(mediaPlayer.isPlaying(), equalTo(true));
        assertThat(mediaPlayer.getCurrentPosition(), equalTo(8000));
    }

    @Test
    public void swipingPosition_whenPaused_justChangesPosition() throws Exception {
        AudioControllerView view = createAndSetupView("clip1", 14000);
        view.findViewById(R.id.playBtn).performClick();
        view.findViewById(R.id.playBtn).performClick();

        SeekBar seekBar = view.findViewById(R.id.seekBar);
        final View currentDuration = view.findViewById(R.id.currentDuration);

        shadowOf(seekBar).getOnSeekBarChangeListener().onStartTrackingTouch(seekBar);

        shadowOf(seekBar).getOnSeekBarChangeListener().onProgressChanged(seekBar, 7000, true);
        fakeScheduler.runForeground();
        assertThat(innerText(currentDuration), equalTo("00:07"));

        shadowOf(seekBar).getOnSeekBarChangeListener().onProgressChanged(seekBar, 8000, true);
        fakeScheduler.runForeground();
        assertThat(innerText(currentDuration), equalTo("00:08"));

        shadowOf(seekBar).getOnSeekBarChangeListener().onStopTrackingTouch(seekBar);
        fakeScheduler.runForeground();
        assertThat(shadowOf(mediaPlayer).getState(), equalTo(ShadowMediaPlayer.State.PAUSED));

        view.findViewById(R.id.playBtn).performClick();

        fakeScheduler.runForeground();
        assertThat(mediaPlayer.getCurrentPosition(), equalTo(8000));
    }

    @Test
    public void resetsBackToBeginningWhenPlaybackFinishes() throws Exception {
        AudioControllerView view = createAndSetupView("clip1", 14000);

        view.findViewById(R.id.playBtn).performClick();

        shadowOf(mediaPlayer).invokeCompletionListener();
        assertThat(getCreatedFromResId(view.findViewById(R.id.playBtn)), equalTo(R.drawable.ic_play_arrow_24dp));
        assertThat(innerText(view.findViewById(R.id.currentDuration)), equalTo("00:00"));
    }

    @Test
    public void whenThereAreTwoViews_showsCorrectDuration() throws Exception {
        AudioControllerView view1 = createAndSetupView("clip1", 14000);
        final AudioControllerView view2 = createAndSetupView("clip2", 14000);

        view1.findViewById(R.id.playBtn).performClick();

        shadowOf(mediaPlayer).setCurrentPosition(1005);

        fakeScheduler.runForeground();
        assertThat(innerText(view1.findViewById(R.id.currentDuration)), equalTo("00:01"));
        assertThat(innerText(view2.findViewById(R.id.currentDuration)), equalTo("00:00"));
    }

    @NotNull
    private AudioControllerView createAndSetupView(String clipID, int fileLength) throws IOException {
        String testFile = File.createTempFile("audio", ".mp3").getAbsolutePath();
        setupMediaPlayerDataSource(testFile, fileLength);

        AudioControllerView view = new AudioControllerView(activity);
        audioHelper.setAudio(view, new Clip(clipID, testFile));
        return view;
    }
}
