package org.odk.collect.android.audio;

import android.media.MediaPlayer;

import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.OnLifecycleEvent;
import androidx.lifecycle.ViewModelProvider;

import org.odk.collect.android.widgets.utilities.AudioPlayer;
import org.odk.collect.async.Scheduler;
import org.odk.collect.audioclips.AudioClipViewModel;
import org.odk.collect.audioclips.Clip;

import java.util.List;
import java.util.function.Supplier;

/**
 * Object for setting up playback of audio clips with {@link AudioButton} and
 * controls. Only one clip can be played at once so when a clip is
 * played from a view or from the `play` method any currently playing audio will stop.
 * <p>
 * Clips are identified using a `clipID` which enables the playback state of clips to survive
 * configuration changes etc. Two views should not use the same `clipID` unless they are intended
 * to have the same playback state i.e. when one is played the other also appears to be playing.
 * This allows for different controls to play the same file but not appear to all be playing at once.
 * <p>
 * An {@link AudioHelper} instance is designed to live at an {@link android.app.Activity} level.
 * However, the underlying implementation uses a {@link androidx.lifecycle.ViewModel} so it is safe to
 * construct multiple instances (within a {@link android.view.View} or
 * {@link androidx.fragment.app.Fragment} for instance) if needed within one
 * {@link android.app.Activity}.
 *
 * @deprecated wrapping the ViewModel like this doesn't really fit with other ways we've integrated
 * widgets with "external" services. Instead of this widgets should talk to {@link AudioPlayer}
 * and the Activity/Fragment components should talk to the ViewModel itself.
 */

@Deprecated
public class AudioHelper {

    private final LifecycleOwner lifecycleOwner;
    private final AudioClipViewModel viewModel;

    public AudioHelper(FragmentActivity activity, LifecycleOwner lifecycleOwner, Scheduler scheduler, Supplier<MediaPlayer> mediaPlayerFactory) {
        this.lifecycleOwner = lifecycleOwner;

        AudioClipViewModel.Factory factory = new AudioClipViewModel.Factory(mediaPlayerFactory, scheduler);

        viewModel = new ViewModelProvider(activity, factory).get(AudioClipViewModel.class);

        registerLifecycleCallbacks(activity, lifecycleOwner);
    }

    /**
     * @param button The control being used for playback
     * @param clip   The clip to be played
     * @return A {@link LiveData} value representing whether this clip is playing or not
     */
    public LiveData<Boolean> setAudio(AudioButton button, Clip clip) {
        AudioClipViewModel viewModel = this.viewModel;

        LiveData<Boolean> isPlaying = viewModel.isPlaying(clip.getClipID());

        isPlaying.observe(lifecycleOwner, button::setPlaying);
        button.setListener(new AudioButtonListener(viewModel, clip.getURI(), clip.getClipID()));

        return isPlaying;
    }

    public void play(Clip clip) {
        viewModel.play(clip);
    }

    public void playInOrder(List<Clip> clips) {
        viewModel.playInOrder(clips);
    }

    public void stop() {
        viewModel.stop();
    }

    public LiveData<Exception> getError() {
        return viewModel.getError();
    }

    public void errorDisplayed() {
        viewModel.errorDisplayed();
    }

    private void registerLifecycleCallbacks(FragmentActivity activity, LifecycleOwner lifecycleOwner) {
        activity.getLifecycle().addObserver(new BackgroundObserver(viewModel));
        lifecycleOwner.getLifecycle().addObserver(new BackgroundObserver(viewModel));
    }

    private static class AudioButtonListener implements AudioButton.Listener {

        private final AudioClipViewModel viewModel;
        private final String uri;
        private final String buttonID;

        AudioButtonListener(AudioClipViewModel viewModel, String uri, String buttonID) {
            this.viewModel = viewModel;
            this.uri = uri;
            this.buttonID = buttonID;
        }

        @Override
        public void onPlayClicked() {
            viewModel.play(new Clip(buttonID, uri));
        }

        @Override
        public void onStopClicked() {
            viewModel.stop();
        }
    }

    private static class BackgroundObserver implements LifecycleObserver {

        private final AudioClipViewModel viewModel;

        BackgroundObserver(AudioClipViewModel viewModel) {
            this.viewModel = viewModel;
        }

        @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
        void onPause() {
            viewModel.background();
        }
    }
}
