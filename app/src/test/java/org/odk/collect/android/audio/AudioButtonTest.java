package org.odk.collect.android.audio;

import android.app.Activity;

import androidx.fragment.app.FragmentActivity;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.odk.collect.android.R;
import org.odk.collect.testshared.RobolectricHelpers;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.odk.collect.testshared.RobolectricHelpers.getCreatedFromResId;

@RunWith(AndroidJUnit4.class)
public class AudioButtonTest {

    private AudioButton button;

    @Before
    public void setup() {
        Activity activity = RobolectricHelpers.createThemedActivity(FragmentActivity.class, R.style.Theme_MaterialComponents);
        button = new AudioButton(activity);
    }

    @Test
    public void isPlayingReturnsFalse_andShowsPlayIcon() {
        assertThat(button.isPlaying(), equalTo(false));
        assertThat(getCreatedFromResId(button.getIcon()), equalTo(R.drawable.ic_volume_up_black_24dp));
    }

    @Test
    public void whenPlayingIsTrue_showsPlayingIcon() {
        button.setPlaying(true);
        assertThat(getCreatedFromResId(button.getIcon()), equalTo(R.drawable.ic_stop_black_24dp));
    }

    @Test
    public void whenPlayingIsFalse_showsPlayIcon() {
        button.setPlaying(false);
        assertThat(getCreatedFromResId(button.getIcon()), equalTo(R.drawable.ic_volume_up_black_24dp));
    }
}
