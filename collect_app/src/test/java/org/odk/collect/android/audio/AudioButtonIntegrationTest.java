package org.odk.collect.android.audio;

import android.media.MediaPlayer;

import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.LiveData;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.odk.collect.android.support.LiveDataTester;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.shadows.ShadowMediaPlayer;
import org.robolectric.shadows.util.DataSource;

import java.io.File;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.robolectric.Shadows.shadowOf;

@RunWith(RobolectricTestRunner.class)
public class AudioButtonIntegrationTest {

    private final MediaPlayer mediaPlayer = new MediaPlayer();
    private final LiveDataTester liveDataTester = new LiveDataTester();

    private FragmentActivity activity;

    @Before
    public void setup() {
        activity = Robolectric.setupActivity(FragmentActivity.class);
    }

    @After
    public void teardown() {
        liveDataTester.teardown();
    }

    @Test
    public void canPlayAndStopAudio() throws Exception {
        String testFile = File.createTempFile("audio", ".mp3").getAbsolutePath();
        final DataSource dataSource = setupDataSource(testFile);

        AudioButton button = new AudioButton(activity);
        AudioButtons.setAudio(button, testFile, activity, () -> mediaPlayer);

        assertThat(getCreatedFromResId(button), equalTo(android.R.drawable.ic_lock_silent_mode_off));

        button.performClick();

        assertThat(mediaPlayer.isPlaying(), is(true));
        assertThat(shadowOf(mediaPlayer).getDataSource(), equalTo(dataSource));
        assertThat(getCreatedFromResId(button), equalTo(android.R.drawable.ic_media_pause));

        button.performClick();

        assertThat(mediaPlayer.isPlaying(), is(false));
        assertThat(getCreatedFromResId(button), equalTo(android.R.drawable.ic_lock_silent_mode_off));
    }

    @Test
    public void playingAudio_stopsOtherAudio() throws Exception {
        String testFile1 = File.createTempFile("audio1", ".mp3").getAbsolutePath();
        String testFile2 = File.createTempFile("audio2", ".mp3").getAbsolutePath();
        setupDataSource(testFile1);
        final DataSource dataSource2 = setupDataSource(testFile2);

        AudioButton button1 = new AudioButton(activity);
        AudioButtons.setAudio(button1, testFile1, activity, () -> mediaPlayer);

        AudioButton button2 = new AudioButton(activity);
        AudioButtons.setAudio(button2, testFile2, activity, () -> mediaPlayer);

        button1.performClick();
        button2.performClick();

        assertThat(mediaPlayer.isPlaying(), is(true));
        assertThat(shadowOf(mediaPlayer).getDataSource(), equalTo(dataSource2));
        assertThat(getCreatedFromResId(button2), equalTo(android.R.drawable.ic_media_pause));
        assertThat(getCreatedFromResId(button1), equalTo(android.R.drawable.ic_lock_silent_mode_off));
    }

    @Test
    public void whenTwoButtonsUseTheSameFile_andOneisPlayed_theyDontBothPlay() throws Exception {
        String testFile1 = File.createTempFile("audio1", ".mp3").getAbsolutePath();
        setupDataSource(testFile1);

        AudioButton button1 = new AudioButton(activity);
        AudioButtons.setAudio(button1, testFile1, activity, () -> mediaPlayer);

        AudioButton button2 = new AudioButton(activity);
        AudioButtons.setAudio(button2, testFile1, activity, () -> mediaPlayer);

        button2.performClick();

        assertThat(getCreatedFromResId(button1), equalTo(android.R.drawable.ic_lock_silent_mode_off));
        assertThat(getCreatedFromResId(button2), equalTo(android.R.drawable.ic_media_pause));
    }

    @Test
    public void setAudio_returnsIsPlayingStateForButton() throws Exception {
        String testFile1 = File.createTempFile("audio1", ".mp3").getAbsolutePath();
        setupDataSource(testFile1);

        AudioButton button1 = new AudioButton(activity);
        LiveData<Boolean> isPlaying = liveDataTester.activate(AudioButtons.setAudio(button1, testFile1, activity, () -> mediaPlayer));

        assertThat(isPlaying.getValue(), equalTo(false));

        button1.performClick();
        assertThat(isPlaying.getValue(), equalTo(true));

        button1.performClick();
        assertThat(isPlaying.getValue(), equalTo(false));
    }

    private DataSource setupDataSource(String testFile) {
        DataSource dataSource = DataSource.toDataSource(testFile);
        ShadowMediaPlayer.addMediaInfo(dataSource, new ShadowMediaPlayer.MediaInfo());
        return dataSource;
    }

    private int getCreatedFromResId(AudioButton button) {
        return shadowOf(button.getDrawable()).getCreatedFromResId();
    }
}