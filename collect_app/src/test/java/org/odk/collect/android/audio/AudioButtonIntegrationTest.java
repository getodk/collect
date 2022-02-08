package org.odk.collect.android.audio;

import android.media.MediaPlayer;

import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.LiveData;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.odk.collect.audioclips.Clip;
import org.odk.collect.androidtest.FakeLifecycleOwner;
import org.odk.collect.testshared.FakeScheduler;
import org.odk.collect.androidtest.LiveDataTester;
import org.robolectric.Robolectric;
import org.robolectric.android.controller.ActivityController;
import org.robolectric.shadows.ShadowMediaPlayer;
import org.robolectric.shadows.util.DataSource;

import java.io.File;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.odk.collect.testshared.RobolectricHelpers.setupMediaPlayerDataSource;
import static org.robolectric.Shadows.shadowOf;

@RunWith(AndroidJUnit4.class)
public class AudioButtonIntegrationTest {

    private final MediaPlayer mediaPlayer = new MediaPlayer();
    private final LiveDataTester liveDataTester = new LiveDataTester();

    private FragmentActivity activity;
    private ActivityController<FragmentActivity> activityController;
    private AudioHelper audioHelper;
    private FakeLifecycleOwner fakeLifecycleOwner;
    private FakeScheduler fakeScheduler;

    @Before
    public void setup() {
        activityController = Robolectric.buildActivity(FragmentActivity.class);
        activity = activityController.setup().get();
        activity.setTheme(com.google.android.material.R.style.Theme_MaterialComponents);

        fakeLifecycleOwner = new FakeLifecycleOwner();
        fakeScheduler = new FakeScheduler();
        audioHelper = new AudioHelper(activity, fakeLifecycleOwner, fakeScheduler, () -> mediaPlayer);
    }

    @After
    public void teardown() {
        liveDataTester.teardown();
    }

    @Test
    public void canPlayAndStopAudio() throws Exception {
        String testFile = File.createTempFile("audio", ".mp3").getAbsolutePath();
        final DataSource dataSource = setupMediaPlayerDataSource(testFile);

        AudioButton button = new AudioButton(activity);
        audioHelper.setAudio(button, new Clip("clip1", testFile));

        assertThat(button.isPlaying(), equalTo(false));

        button.performClick();

        assertThat(mediaPlayer.isPlaying(), is(true));
        assertThat(shadowOf(mediaPlayer).getDataSource(), equalTo(dataSource));
        assertThat(button.isPlaying(), equalTo(true));

        button.performClick();

        assertThat(mediaPlayer.isPlaying(), is(false));
        assertThat(button.isPlaying(), equalTo(false));
    }

    @Test
    public void playingAudio_stopsOtherAudio() throws Exception {
        String testFile1 = File.createTempFile("audio1", ".mp3").getAbsolutePath();
        String testFile2 = File.createTempFile("audio2", ".mp3").getAbsolutePath();
        setupMediaPlayerDataSource(testFile1);
        final DataSource dataSource2 = setupMediaPlayerDataSource(testFile2);

        AudioButton button1 = new AudioButton(activity);
        audioHelper.setAudio(button1, new Clip("clip1", testFile1));

        AudioButton button2 = new AudioButton(activity);
        audioHelper.setAudio(button2, new Clip("clip2", testFile2));

        button1.performClick();
        button2.performClick();

        assertThat(mediaPlayer.isPlaying(), is(true));
        assertThat(shadowOf(mediaPlayer).getDataSource(), equalTo(dataSource2));
        assertThat(button2.isPlaying(), equalTo(true));
        assertThat(button1.isPlaying(), equalTo(false));
    }

    @Test
    public void whenTwoButtonsUseTheSameFileButDifferentClipIDs_andOneisPlayed_theyDontBothPlay() throws Exception {
        String testFile1 = File.createTempFile("audio1", ".mp3").getAbsolutePath();
        setupMediaPlayerDataSource(testFile1);

        AudioButton button1 = new AudioButton(activity);
        audioHelper.setAudio(button1, new Clip("clip1", testFile1));

        AudioButton button2 = new AudioButton(activity);
        audioHelper.setAudio(button2, new Clip("clip2", testFile1));

        button2.performClick();

        assertThat(button1.isPlaying(), equalTo(false));
        assertThat(button2.isPlaying(), equalTo(true));
    }

    @Test
    public void pausingActivity_releaseMediaPlayer() throws Exception {
        String testFile1 = File.createTempFile("audio1", ".mp3").getAbsolutePath();
        setupMediaPlayerDataSource(testFile1);

        AudioButton button = new AudioButton(activity);
        audioHelper.setAudio(button, new Clip("clip1", testFile1));

        activityController.pause();

        assertThat(shadowOf(mediaPlayer).getState(), equalTo(ShadowMediaPlayer.State.END));
    }

    @Test
    public void pausingAndResumingActivity_andThenPressingPlay_startsClipFromTheBeginning() throws Exception {
        String testFile1 = File.createTempFile("audio1", ".mp3").getAbsolutePath();
        setupMediaPlayerDataSource(testFile1);

        AudioButton button = new AudioButton(activity);
        audioHelper.setAudio(button, new Clip("clip1", testFile1));

        button.performClick();
        shadowOf(mediaPlayer).setCurrentPosition(1000);
        fakeScheduler.runForeground();

        activityController.pause();
        activityController.resume();

        button.performClick();
        assertThat(mediaPlayer.getCurrentPosition(), equalTo(0));
    }

    @Test
    public void destroyingLifecycle_releaseMediaPlayer() throws Exception {
        String testFile1 = File.createTempFile("audio1", ".mp3").getAbsolutePath();
        setupMediaPlayerDataSource(testFile1);

        AudioButton button = new AudioButton(activity);
        audioHelper.setAudio(button, new Clip("clip1", testFile1));

        fakeLifecycleOwner.destroy();

        assertThat(shadowOf(mediaPlayer).getState(), equalTo(ShadowMediaPlayer.State.END));
    }

    @Test
    public void setAudio_returnsIsPlayingStateForButton() throws Exception {
        String testFile1 = File.createTempFile("audio1", ".mp3").getAbsolutePath();
        setupMediaPlayerDataSource(testFile1);

        AudioButton button1 = new AudioButton(activity);
        LiveData<Boolean> isPlaying = liveDataTester.activate(audioHelper.setAudio(button1, new Clip("clip1", testFile1)));

        assertThat(isPlaying.getValue(), equalTo(false));

        button1.performClick();
        assertThat(isPlaying.getValue(), equalTo(true));

        button1.performClick();
        assertThat(isPlaying.getValue(), equalTo(false));
    }
}
