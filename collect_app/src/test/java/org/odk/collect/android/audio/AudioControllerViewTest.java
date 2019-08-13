package org.odk.collect.android.audio;

import androidx.fragment.app.FragmentActivity;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.odk.collect.android.R;
import org.odk.collect.android.support.RobolectricHelpers;
import org.robolectric.RobolectricTestRunner;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@RunWith(RobolectricTestRunner.class)
public class AudioControllerViewTest {

    @Test
    public void fastFwdAndFastRewind_whenNotPlaying_dontCallListener() {
        FragmentActivity activity = RobolectricHelpers.buildThemedActivity(FragmentActivity.class).get();
        AudioControllerView view = new AudioControllerView(activity);

        AudioControllerView.Listener listener = mock(AudioControllerView.Listener.class);
        view.setListener(listener);

        view.setPlayState(AudioPlayerViewModel.ClipState.NOT_PLAYING);

        view.findViewById(R.id.fastForwardBtn).performClick();
        verify(listener, never()).onPositionChanged(any());

        view.findViewById(R.id.fastRewindBtn).performClick();
        verify(listener, never()).onPositionChanged(any());
    }
}