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
import org.odk.collect.android.utilities.Scheduler;
import org.odk.collect.android.utilities.TimerScheduler;

import static androidx.lifecycle.Transformations.map;
import static org.odk.collect.android.audio.AudioPlayerViewModel.ClipState;
import static org.odk.collect.android.audio.AudioPlayerViewModel.ClipState.PLAYING;

public class AudioHelper {

    private final MediaPlayerFactory mediaPlayerFactory;
    private final Scheduler scheduler;
    private final FragmentActivity activity;
    private final LifecycleOwner lifecycleOwner;

    public AudioHelper(FragmentActivity activity, LifecycleOwner lifecycleOwner, Scheduler scheduler, MediaPlayerFactory mediaPlayerFactory) {
        this.activity = activity;
        this.lifecycleOwner = lifecycleOwner;
        this.mediaPlayerFactory = mediaPlayerFactory;
        this.scheduler = scheduler;
    }

    public AudioHelper(FragmentActivity activity, LifecycleOwner lifecycleOwner) {
        this(activity, lifecycleOwner, new TimerScheduler(), MediaPlayer::new);
    }

    public LiveData<Boolean> setAudio(AudioButton button, String uri, String clipID) {
        AudioPlayerViewModel viewModel = getViewModel();

        LiveData<Boolean> isPlaying = map(viewModel.isPlaying(clipID), value -> value == PLAYING);

        isPlaying.observe(lifecycleOwner, button::setPlaying);
        button.setListener(new AudioButtonListener(viewModel, uri, clipID));

        return isPlaying;
    }

    public void setAudio(AudioControllerView view, String uri, String clipID) {
        AudioPlayerViewModel viewModel = getViewModel();

        LiveData<ClipState> playState = viewModel.isPlaying(clipID);
        LiveData<Integer> position = viewModel.getPosition(clipID);

        playState.observe(lifecycleOwner, view::setPlayState);
        position.observe(lifecycleOwner, view::setPosition);
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
