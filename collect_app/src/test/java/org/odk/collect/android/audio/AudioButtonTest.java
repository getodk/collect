package org.odk.collect.android.audio;

import androidx.fragment.app.FragmentActivity;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.odk.collect.android.support.RobolectricHelpers.getCreatedFromResId;

@RunWith(RobolectricTestRunner.class)
public class AudioButtonTest {

    @Test
    public void isPlayingReturnsFalse_andShowsPlayIcon() {
        AudioButton button = new AudioButton(Robolectric.setupActivity(FragmentActivity.class));
        assertThat(button.isPlaying(), equalTo(false));
        assertThat(getCreatedFromResId(button), equalTo(android.R.drawable.ic_lock_silent_mode_off));
    }

    @Test
    public void whenPlayingIsTrue_showsPlayingIcon() {
        AudioButton button = new AudioButton(Robolectric.setupActivity(FragmentActivity.class));

        button.setPlaying(true);
        assertThat(getCreatedFromResId(button), equalTo(android.R.drawable.ic_media_pause));
    }

    @Test
    public void whenPlayingIsFalse_showsPlayIcon() {
        AudioButton button = new AudioButton(Robolectric.setupActivity(FragmentActivity.class));

        button.setPlaying(false);
        assertThat(getCreatedFromResId(button), equalTo(android.R.drawable.ic_lock_silent_mode_off));
    }
}