package org.odk.collect.android.audio;

import androidx.fragment.app.FragmentActivity;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.odk.collect.android.R;
import org.robolectric.RobolectricTestRunner;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.odk.collect.android.support.RobolectricHelpers.buildThemedActivity;
import static org.robolectric.shadows.ShadowView.innerText;

@RunWith(RobolectricTestRunner.class)
public class AudioControllerViewTest {

    @Test
    public void clickingFastForward_whenPostionAtDuration_skipsToDuration() {
        FragmentActivity activity = buildThemedActivity(FragmentActivity.class).get();
        AudioControllerView view = new AudioControllerView(activity);
        AudioControllerView.Listener listener = mock(AudioControllerView.Listener.class);

        view.setListener(listener);
        view.setDuration(1000);
        view.setPosition(1000);

        view.findViewById(R.id.fastForwardBtn).performClick();

        assertThat(innerText(view.findViewById(R.id.currentDuration)), equalTo("00:01"));
        verify(listener).onPositionChanged(1000);
    }

    @Test
    public void clickingFastRewind_whenPostionAtZero_skipsTo0() {
        FragmentActivity activity = buildThemedActivity(FragmentActivity.class).get();
        AudioControllerView view = new AudioControllerView(activity);
        AudioControllerView.Listener listener = mock(AudioControllerView.Listener.class);

        view.setListener(listener);
        view.setDuration(1000);
        view.setPosition(0);

        view.findViewById(R.id.fastRewindBtn).performClick();

        assertThat(innerText(view.findViewById(R.id.currentDuration)), equalTo("00:00"));
        verify(listener).onPositionChanged(0);
    }
}