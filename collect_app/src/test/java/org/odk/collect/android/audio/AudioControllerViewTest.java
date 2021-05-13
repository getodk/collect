 package org.odk.collect.android.audio;

import android.widget.SeekBar;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.odk.collect.android.R;
import org.odk.collect.android.support.SwipableParentActivity;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.odk.collect.android.support.CollectHelpers.buildThemedActivity;
import static org.robolectric.Shadows.shadowOf;
import static org.robolectric.shadows.ShadowView.innerText;

@RunWith(AndroidJUnit4.class)
public class AudioControllerViewTest {

    private SwipableParentActivity activity;
    private AudioControllerView view;

    @Before
    public void setup() {
        activity = buildThemedActivity(SwipableParentActivity.class).get();
        view = new AudioControllerView(activity);
    }

    @Test
    public void setDuration_showsDurationInMinutesAndSeconds() {
        view.setDuration(52000);
        assertThat(view.binding.totalDuration.getText().toString(), equalTo("00:52"));
    }

    @Test
    public void setPosition_showsPositionInMinutesAndSeconds() {
        view.setDuration(65000);

        view.setPosition(64000);
        assertThat(view.binding.currentDuration.getText().toString(), equalTo("01:04"));
    }

    @Test
    public void setPosition_changesSeekBarPosition() {
        view.setDuration(65000);
        assertThat(view.binding.seekBar.getProgress(), is(0));
        assertThat(view.binding.seekBar.getMax(), is(65000));

        view.setPosition(8000);
        assertThat(view.binding.seekBar.getProgress(), is(8000));
        assertThat(view.binding.seekBar.getMax(), is(65000));
    }

    @Test
    public void swipingSeekBar_whenPaused_skipsToPositionOnceStopped() {
        AudioControllerView.Listener listener = mock(AudioControllerView.Listener.class);

        view.setListener(listener);
        view.setDuration(12000);

        SeekBar seekBar = view.binding.seekBar;
        shadowOf(seekBar).getOnSeekBarChangeListener().onStartTrackingTouch(seekBar);

        shadowOf(seekBar).getOnSeekBarChangeListener().onProgressChanged(seekBar, 7000, true);
        assertThat(innerText(view.findViewById(R.id.currentDuration)), equalTo("00:07"));
        verifyNoInteractions(listener); // We don't change position yet

        shadowOf(seekBar).getOnSeekBarChangeListener().onProgressChanged(seekBar, 5000, true);
        assertThat(innerText(view.findViewById(R.id.currentDuration)), equalTo("00:05"));
        verifyNoInteractions(listener); // We don't change position yet

        shadowOf(seekBar).getOnSeekBarChangeListener().onStopTrackingTouch(seekBar);
        assertThat(innerText(view.findViewById(R.id.currentDuration)), equalTo("00:05"));
        verify(listener).onPositionChanged(5000);
    }

    @Test
    public void swipingSeekBar_whenPlaying_pauses_andThenSkipsToPositionAndPlaysOnceStopped() {
        AudioControllerView.Listener listener = mock(AudioControllerView.Listener.class);

        view.setListener(listener);
        view.setDuration(12000);
        view.setPlaying(true);

        SeekBar seekBar = view.binding.seekBar;
        shadowOf(seekBar).getOnSeekBarChangeListener().onStartTrackingTouch(seekBar);
        verify(listener).onPauseClicked();

        shadowOf(seekBar).getOnSeekBarChangeListener().onProgressChanged(seekBar, 7000, true);
        assertThat(innerText(view.findViewById(R.id.currentDuration)), equalTo("00:07"));
        verifyNoMoreInteractions(listener); // We don't change position yet

        shadowOf(seekBar).getOnSeekBarChangeListener().onProgressChanged(seekBar, 5000, true);
        assertThat(innerText(view.findViewById(R.id.currentDuration)), equalTo("00:05"));
        verifyNoMoreInteractions(listener); // We don't change position yet

        shadowOf(seekBar).getOnSeekBarChangeListener().onStopTrackingTouch(seekBar);
        assertThat(innerText(view.findViewById(R.id.currentDuration)), equalTo("00:05"));
        verify(listener).onPositionChanged(5000);
        verify(listener).onPlayClicked();
    }

    @Test
    public void whenSwiping_notifiesSwipeableParent() {
        SeekBar seekBar = view.findViewById(R.id.seekBar);

        shadowOf(seekBar).getOnSeekBarChangeListener().onStartTrackingTouch(seekBar);
        assertThat(activity.isSwipingAllowed(), equalTo(false));

        shadowOf(seekBar).getOnSeekBarChangeListener().onStopTrackingTouch(seekBar);
        assertThat(activity.isSwipingAllowed(), equalTo(true));
    }
}
