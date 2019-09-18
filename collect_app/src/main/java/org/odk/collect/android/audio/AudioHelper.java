package org.odk.collect.android.audio;

import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;

import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.OnLifecycleEvent;
import androidx.lifecycle.ViewModelProviders;

import org.jetbrains.annotations.NotNull;
import org.odk.collect.android.utilities.HandlerScheduler;
import org.odk.collect.android.utilities.Scheduler;

/**
 * Object for setting up playback of audio clips with {@link AudioButton} and
 * {@link AudioControllerView} controls. Only one clip can be played at once so when a clip is
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
 */

public class AudioHelper {

    private final MediaPlayerFactory mediaPlayerFactory;
    private final Scheduler scheduler;
    private final FragmentActivity activity;
    private final LifecycleOwner lifecycleOwner;

    /**
     * @param activity       The activity where controls will be displayed
     * @param lifecycleOwner A representative lifecycle for controls - allows for differing activity and control lifecycle
     */
    public AudioHelper(FragmentActivity activity, LifecycleOwner lifecycleOwner) {
        this(activity, lifecycleOwner, new HandlerScheduler(), MediaPlayer::new);
    }

    AudioHelper(FragmentActivity activity, LifecycleOwner lifecycleOwner, Scheduler scheduler, MediaPlayerFactory mediaPlayerFactory) {
        this.activity = activity;
        this.lifecycleOwner = lifecycleOwner;
        this.mediaPlayerFactory = mediaPlayerFactory;
        this.scheduler = scheduler;
    }

    /**
     * @param button The control being used for playback
     * @param uri    The location of the clip
     * @param clipID An identifier for this clip
     * @return A {@link LiveData} value representing whether this clip is playing or not
     */
    public LiveData<Boolean> setAudio(AudioButton button, String uri, String clipID) {
        AudioPlayerViewModel viewModel = getViewModel();

        LiveData<Boolean> isPlaying = viewModel.isPlaying(clipID);

        isPlaying.observe(lifecycleOwner, button::setPlaying);
        button.setListener(new AudioButtonListener(viewModel, uri, clipID));

        return isPlaying;
    }

    /**
     * @param view   The control being used for playback
     * @param uri    The location of the clip
     * @param clipID An identifier for this clip
     */
    public void setAudio(AudioControllerView view, String uri, String clipID) {
        AudioPlayerViewModel viewModel = getViewModel();

        viewModel.isPlaying(clipID).observe(lifecycleOwner, view::setPlaying);
        viewModel.getPosition(clipID).observe(lifecycleOwner, view::setPosition);
        view.setDuration(getDurationOfFile(uri));
        view.setListener(new AudioControllerViewListener(viewModel, uri, clipID));
    }

    public void play(String clipID, String uri) {
        getViewModel().play(clipID, uri);
    }

    private Integer getDurationOfFile(String uri) {
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(uri);
        String durationString = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
        return durationString != null ? Integer.parseInt(durationString) : 0;
    }

    @NotNull
    private AudioPlayerViewModel getViewModel() {
        AudioPlayerViewModelFactory factory = new AudioPlayerViewModelFactory(mediaPlayerFactory, scheduler);

        AudioPlayerViewModel viewModel = ViewModelProviders
                .of(activity, factory)
                .get(AudioPlayerViewModel.class);

        activity.getLifecycle().addObserver(new BackgroundObserver(viewModel));
        lifecycleOwner.getLifecycle().addObserver(new BackgroundObserver(viewModel));

        return viewModel;
    }

    public LiveData<Exception> getError() {
        return getViewModel().getError();
    }

    private static class AudioControllerViewListener implements AudioControllerView.Listener {

        private final AudioPlayerViewModel viewModel;
        private final String uri;
        private final String clipID;

        AudioControllerViewListener(AudioPlayerViewModel viewModel, String uri, String clipID) {
            this.viewModel = viewModel;
            this.uri = uri;
            this.clipID = clipID;
        }

        @Override
        public void onPlayClicked() {
            viewModel.play(clipID, uri);
        }

        @Override
        public void onPauseClicked() {
            viewModel.pause();
        }

        @Override
        public void onPositionChanged(Integer newPosition) {
            viewModel.setPosition(clipID, newPosition);
        }
    }

    private static class AudioButtonListener implements AudioButton.Listener {

        private final AudioPlayerViewModel viewModel;
        private final String uri;
        private final String buttonID;

        AudioButtonListener(AudioPlayerViewModel viewModel, String uri, String buttonID) {
            this.viewModel = viewModel;
            this.uri = uri;
            this.buttonID = buttonID;
        }

        @Override
        public void onPlayClicked() {
            viewModel.play(buttonID, uri);
        }

        @Override
        public void onStopClicked() {
            viewModel.stop();
        }
    }

    private static class BackgroundObserver implements LifecycleObserver {

        private final AudioPlayerViewModel viewModel;

        BackgroundObserver(AudioPlayerViewModel viewModel) {
            this.viewModel = viewModel;
        }

        @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
        void onPause() {
            viewModel.background();
        }
    }
}
