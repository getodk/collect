/*
 * Copyright (C) 2018 Shobhit Agarwal
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package org.odk.collect.android.widgets;

import android.content.Context;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.odk.collect.android.R;
import org.odk.collect.android.activities.FormEntryActivity;
import org.odk.collect.android.controller.MediaController;
import org.odk.collect.android.events.MediaEvent;
import org.odk.collect.android.events.RxEventBus;

import java.io.File;
import java.io.IOException;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;

import static android.view.View.GONE;

public class AudioController implements SeekBar.OnSeekBarChangeListener {

    private static final int SEEK_FORWARD_TIME = 5000; // 5 seconds
    private static final int SEEK_BACKWARD_TIME = 5000; // 5 seconds
    private final Handler seekHandler = new Handler();

    @BindView(R.id.currentDuration)
    TextView currentDurationLabel;
    @BindView(R.id.totalDuration)
    TextView totalDurationLabel;
    @BindView(R.id.playBtn)
    ImageButton playButton;
    @BindView(R.id.seekBar)
    SeekBar seekBar;

    private View view;
    private Context context;
    private Disposable disposable;
    private MediaController mediaController;

    /**
     * Background Runnable thread
     */
    private final Runnable updateTimeTask = new Runnable() {
        public void run() {
            try {
                if (mediaController.isPlaying()) {
                    updateTimer();
                    seekHandler.postDelayed(this, 100);
                } else {
                    seekBar.setProgress(mediaController.getDuration());
                    seekHandler.removeCallbacks(updateTimeTask);
                }
            } catch (IllegalStateException e) {
                seekHandler.removeCallbacks(updateTimeTask);
                Timber.i(e, "Attempting to update timer when player is stopped");
            }
        }
    };

    /**
     * Converts {@param millis} to mm:ss format
     *
     * @return formatted time as string
     */
    private static String getTime(long millis) {
        return new DateTime(millis, DateTimeZone.UTC).toString("mm:ss");
    }

    void init(Context context, MediaController mediaController, RxEventBus rxEventBus) {
        this.context = context;
        this.mediaController = mediaController;

        initMediaPlayer(rxEventBus);
    }

    @OnClick(R.id.fastForwardBtn)
    void fastForwardMedia() {
        seekTo(mediaController.getCurrentPosition() + SEEK_FORWARD_TIME);
    }

    @OnClick(R.id.fastRewindBtn)
    void rewindMedia() {
        seekTo(mediaController.getCurrentPosition() - SEEK_BACKWARD_TIME);
    }

    /**
     * Seeks media to the new position and updates timer
     */
    private void seekTo(int newPosition) {
        mediaController.seekTo(Math.min(Math.max(0, newPosition), mediaController.getDuration()));
        updateTimer();
    }

    @OnClick(R.id.playBtn)
    void playClicked() {
        if (mediaController.isPlaying()) {
            pause();
        } else {
            play();
        }
    }

    private void updateTimer() {
        totalDurationLabel.setText(getTime(mediaController.getDuration()));
        currentDurationLabel.setText(getTime(mediaController.getCurrentPosition()));

        seekBar.setMax(mediaController.getDuration());
        seekBar.setProgress(mediaController.getCurrentPosition());
    }

    private void initControlsLayout(ViewGroup parent) {
        view = LayoutInflater.from(context).inflate(R.layout.media_player_layout, parent, false);
        ButterKnife.bind(this, view);
        seekBar.setOnSeekBarChangeListener(this);
    }

    private void initMediaPlayer(RxEventBus rxEventBus) {
        disposable = rxEventBus.register(MediaEvent.class)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(mediaEvent -> {
                    if (mediaEvent.getResultCode() == MediaController.MEDIA_PREPARED) {
                        updateTimer();
                    } else if (mediaEvent.getResultCode() == MediaController.MEDIA_COMPLETED) {
                        playButton.setImageResource(R.drawable.ic_play_arrow_24dp);
                    }
                }, Timber::e);
    }

    /**
     * Update timer on seekbar
     */
    private void updateProgressBar() {
        seekHandler.postDelayed(updateTimeTask, 100);
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if (fromUser) {
            seekTo(progress);
        }
    }

    /**
     * When user starts moving the progress handler
     */
    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        ((FormEntryActivity) context).allowSwiping(false);

        if (mediaController.isPlaying()) {
            pause();
        }
    }

    /**
     * When user stops moving the progress handler
     */
    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        ((FormEntryActivity) context).allowSwiping(true);

        seekTo(seekBar.getProgress());
        if (mediaController.isPlaying()) {
            play();
        }
    }

    private void play() {
        playButton.setImageResource(R.drawable.ic_pause_24dp);

        if (seekBar.getProgress() == mediaController.getDuration()) {
            seekBar.setProgress(0);
        }

        mediaController.startAudio();
        updateProgressBar();
    }

    private void pause() {
        playButton.setImageResource(R.drawable.ic_play_arrow_24dp);
        mediaController.pauseAudio();
        seekHandler.removeCallbacks(updateTimeTask);
    }

    public void setMedia(File file) {
        try {
            mediaController.setMedia(file);
        } catch (IOException e) {
            Timber.e(e);
        }
    }

    void hidePlayer() {
        view.setVisibility(GONE);
    }

    View getPlayerLayout(ViewGroup parent) {
        initControlsLayout(parent);
        return view;
    }
}
